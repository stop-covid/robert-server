package fr.gouv.stopc.robert.server.batch.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.CollectionUtils;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.server.batch.exception.RobertScoringException;
import fr.gouv.stopc.robert.server.batch.service.ScoringStrategyService;
import fr.gouv.stopc.robert.server.batch.utils.PropertyLoader;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.EpochExposition;
import fr.gouv.stopc.robertserver.database.model.HelloMessageDetail;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.ContactService;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ContactProcessor implements ItemProcessor<Contact, Contact> {

    private IServerConfigurationService serverConfigurationService;

    private IRegistrationService registrationService;

    private ContactService contactService;

    private ICryptoServerGrpcClient cryptoServerClient;

    private ScoringStrategyService scoringStrategy;

    private PropertyLoader propertyLoader;

    /**
     * NOTE:
     * validation step order has evolved from spec because of delegation of validation of messages to crypto back-end
     * @param contact
     * @return
     * @throws RobertServerCryptoException
     * @throws RobertScoringException
     */
    @Override
    public Contact process(Contact contact) throws RobertServerCryptoException, RobertScoringException {
        log.info("Contact processing started");

        if (CollectionUtils.isEmpty(contact.getMessageDetails())) {
            log.warn("No messages in contact; discarding contact");
            this.contactService.delete(contact);
            return null;
        }

        byte[] serverCountryCode = new byte[1];
        serverCountryCode[0] = this.serverConfigurationService.getServerCountryCode();

        Registration registration = null;
        Integer epoch = null;
        for (HelloMessageDetail helloMessageDetail : contact.getMessageDetails()) {
            GetInfoFromHelloMessageRequest request = GetInfoFromHelloMessageRequest.newBuilder()
                    .setEcc(ByteString.copyFrom(contact.getEcc()))
                    .setEbid(ByteString.copyFrom(contact.getEbid()))
                    .setTimeSent(helloMessageDetail.getTimeFromHelloMessage())
                    .setMac(ByteString.copyFrom(helloMessageDetail.getMac()))
                    .setTimeReceived(helloMessageDetail.getTimeCollectedOnDevice())
                    .build();

            // Step #8: Validate message
            Optional<GetInfoFromHelloMessageResponse> response = this.cryptoServerClient.getInfoFromHelloMessage(request);

            if (response.isPresent()) {
                GetInfoFromHelloMessageResponse helloMessageResponse = response.get();

                // Check step #2: is contact managed by this server?
                if (!Arrays.equals(helloMessageResponse.getCountryCode().toByteArray(), serverCountryCode)) {
                    log.info(
                            "Country code {} is not managed by this server ({}); rerouting contact to federation network",
                            helloMessageResponse.getCountryCode(),
                            serverCountryCode);

                    // TODO: send the message to the dedicated country server
                    // remove the message from the database
                    this.contactService.delete(contact);
                    return null;
                } else {
                    byte[] idA = helloMessageResponse.getIdA().toByteArray();
                    epoch = helloMessageResponse.getEpochId();

                    // Check step #4: check once if registration exists
                    if (Objects.isNull(registration)) {
                        Optional<Registration> registrationRecord = registrationService.findById(idA);

                        if (!registrationRecord.isPresent()) {
                            log.info("Recovered id_A is unknown (fake or now unregistered?): {}; discarding contact", idA);
                            contactService.delete(contact);
                            return null;
                        } else {
                            registration = registrationRecord.get();
                        }
                    }

                    // Check steps #5, #6
                    if (!step5CheckDeltaTaAndTimeABelowThreshold(helloMessageDetail)
                        || !step6CheckTimeACorrespondsToEpochiA(
                                helloMessageResponse.getEpochId(),
                                helloMessageDetail.getTimeCollectedOnDevice())) {
                        contactService.delete(contact);
                        return null;
                    }
                }
            } else {
                log.warn("At least one HELLO message could not be validated; discarding contact");
                this.contactService.delete(contact);
                return null;
            }
        }

        List<EpochExposition> epochsToKeep = step9ScoreAndAddContactInListOfExposedEpochs(contact, epoch, registration);
        int latestRiskEpoch = registration.getLatestRiskEpoch();

        // Only consider epochs that are after the last notification for scoring
        List<EpochExposition> scoresSinceLastNotif = CollectionUtils.isEmpty(epochsToKeep) ?
                new ArrayList<>()
                : epochsToKeep.stream()
                .filter(ep -> ep.getEpochId() > latestRiskEpoch)
                .collect(Collectors.toList());

        Double totalRisk = scoresSinceLastNotif.stream()
                .map(EpochExposition::getExpositionScores)
                .map(item -> item.stream().mapToDouble(Double::doubleValue).sum())
                .reduce(0.0, (a,b) -> a + b);

        registration.setAtRisk(totalRisk > this.propertyLoader.getRiskThreshold());

        this.registrationService.saveRegistration(registration);
        this.contactService.delete(contact);
        return null;

    }

    /**
     *  Robert Spec Step #5: check that the delta between tA (16 bits) & timeA (32 bits) [truncated to 16bits] is below threshold.
     */
    private boolean step5CheckDeltaTaAndTimeABelowThreshold(HelloMessageDetail helloMessageDetail) {
        // Process 16-bit values for sanity check
        final long timeFromHelloNTPsecAs16bits = castIntegerToLong(helloMessageDetail.getTimeFromHelloMessage(), 2);
        final long timeFromDeviceAs16bits = castLong(helloMessageDetail.getTimeCollectedOnDevice(), 2);
        final int timeDiffTolerance = this.serverConfigurationService.getHelloMessageTimeStampTolerance();

        // TODO: fix this as overflow of 16bits may cause rejection of valid messages
        if (Math.abs(timeFromHelloNTPsecAs16bits - timeFromDeviceAs16bits) > timeDiffTolerance) {
            log.warn("Time tolerance was exceeded: |{} (HELLO) vs {} (receiving device)| > {}; discarding HELLO message",
                    timeFromHelloNTPsecAs16bits,
                    timeFromDeviceAs16bits,
                    timeDiffTolerance);
            return false;
        }
        return true;
    }

    /**
     *  Robert Spec Step #6
     */
    private boolean step6CheckTimeACorrespondsToEpochiA(int epochId, long timeFromDevice) {
        final long tpstStartNTPsec = this.serverConfigurationService.getServiceTimeStart();
        long epochIdFromMessage = TimeUtils.getNumberOfEpochsBetween(tpstStartNTPsec, timeFromDevice);

        // Check if epochs match with a limited tolerance
        if (Math.abs(epochIdFromMessage - epochId) > 1) {
            log.warn("Epochid from message {}  vs epochid from ebid  {} > 1 (tolerance); discarding HELLO message",
                    epochIdFromMessage,
                    epochId);
            return false;
        }
        return true;
    }

    /**
     * Robert spec Step #9: add i_A in LEE_A
     */
    private List<EpochExposition> step9ScoreAndAddContactInListOfExposedEpochs(Contact contact, int epochIdFromEBID, Registration registrationRecord) throws RobertScoringException {
        List<EpochExposition> exposedEpochs = registrationRecord.getExposedEpochs();

        // Exposed epochs should be empty, never null
        if (Objects.isNull(exposedEpochs)) {
            exposedEpochs = new ArrayList<>();
        }

        // Add EBID's epoch to exposed epochs list
        Optional<EpochExposition> epochToAddTo = exposedEpochs.stream()
                .filter(item -> item.getEpochId() == epochIdFromEBID)
                .findFirst();

        Double scoredRisk =  this.scoringStrategy.execute(contact);
        if (epochToAddTo.isPresent()) {
            List<Double> epochScores = epochToAddTo.get().getExpositionScores();
            epochScores.add(scoredRisk);
        } else {
            exposedEpochs.add(EpochExposition.builder()
                    .expositionScores(Arrays.asList(scoredRisk))
                    .epochId(epochIdFromEBID)
                    .build());
        }

        List<EpochExposition> epochsToKeep = getExposedEpochsWithoutEpochsOlderThanContagiousPeriod(exposedEpochs);
        registrationRecord.setExposedEpochs(epochsToKeep);
        return epochsToKeep;
    }

    /**
     * Keep epochs within the contagious period
     * @param exposedEpochs
     * @return
     */
    private List<EpochExposition> getExposedEpochsWithoutEpochsOlderThanContagiousPeriod(List<EpochExposition> exposedEpochs) {
        int currentEpochId = TimeUtils.getCurrentEpochFrom(this.serverConfigurationService.getServiceTimeStart());

        // Purge exposed epochs list from epochs older than contagious period (C_T)
        return CollectionUtils.isEmpty(exposedEpochs) ?
                new ArrayList<>()
                : exposedEpochs.stream().filter(epoch -> {
            int nbOfEpochsToKeep = (this.serverConfigurationService.getContagiousPeriod() * 24 * 3600)
                    / this.serverConfigurationService.getEpochDurationSecs();
            return (currentEpochId - epoch.getEpochId()) <= nbOfEpochsToKeep;
        }).collect(Collectors.toList());
    }

    private long castIntegerToLong(int x, int nbOfSignificantBytes) {
        int shift = nbOfSignificantBytes * 8;
        return Integer.toUnsignedLong(x << shift >>> shift);
    }

    private long castLong(long x, int nbOfSignificantBytes) {
        int shift = (Long.BYTES - nbOfSignificantBytes) * 8;
        return x << shift >>> shift;
    }
}
