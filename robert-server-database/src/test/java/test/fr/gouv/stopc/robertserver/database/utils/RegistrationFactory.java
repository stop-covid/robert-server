package test.fr.gouv.stopc.robertserver.database.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.gouv.stopc.robertserver.database.model.EpochExposition;
import fr.gouv.stopc.robertserver.database.model.Registration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RegistrationFactory {

	/**
	 * 
	 * @param epochs
	 * @param deltaEpoch
	 * @return
	 */
	public static List<Registration> generateNotifiedRegistration(List<Integer> epochs, int deltaEpoch) {
		return epochs.stream().map(epoch -> {
			SecureRandom sr = new SecureRandom();
			byte[] rndBytes = new byte[5];
			sr.nextBytes(rndBytes);
			return Registration.builder().permanentIdentifier(rndBytes).atRisk(true).isNotified(true)
					.latestRiskEpoch(epoch + deltaEpoch).lastStatusRequestEpoch(epoch + 2 * deltaEpoch).build();
		}).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param epochs
	 * @param deltaEpoch
	 * @return
	 */
	public static List<Registration> generateAtRiskButNotNotifiedRegistration(List<Integer> epochs, int deltaEpoch) {
		return epochs.stream().map(epoch -> {
			SecureRandom sr = new SecureRandom();
			byte[] rndBytes = new byte[5];
			sr.nextBytes(rndBytes);
			return Registration.builder().permanentIdentifier(rndBytes).atRisk(true).isNotified(false)
					.latestRiskEpoch(epoch + deltaEpoch).lastStatusRequestEpoch(epoch + 2 * deltaEpoch).build();
		}).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param epochs
	 * @return
	 */
	public static List<Registration> generateExposedButNotAtRiskRegistration(List<Integer> epochs, int deltaEpoch) {
		List<EpochExposition> fakeExpositions = new ArrayList<>();
		fakeExpositions.add(EpochExposition.builder().epochId(1).build());
		return epochs.stream().map(epoch -> {
			SecureRandom sr = new SecureRandom();
			byte[] rndBytes = new byte[5];
			sr.nextBytes(rndBytes);
			return Registration.builder().permanentIdentifier(rndBytes).atRisk(false).isNotified(true)
					.latestRiskEpoch(epoch + deltaEpoch).lastStatusRequestEpoch(epoch + 2 * deltaEpoch)
					.exposedEpochs(fakeExpositions).build();
		}).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param epochs
	 * @return
	 */
	public List<Registration> generateNotExposedRegistration(List<Integer> epochs, int deltaEpoch) {
		return epochs.stream().map(epoch -> {
			SecureRandom sr = new SecureRandom();
			byte[] rndBytes = new byte[5];
			sr.nextBytes(rndBytes);
			return Registration.builder().permanentIdentifier(rndBytes).atRisk(false).isNotified(true)
					.lastStatusRequestEpoch(epoch + 2 * deltaEpoch).build();
		}).collect(Collectors.toList());
	}
}
