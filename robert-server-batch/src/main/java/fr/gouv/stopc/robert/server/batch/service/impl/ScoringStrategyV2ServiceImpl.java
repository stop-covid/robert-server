package fr.gouv.stopc.robert.server.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.server.batch.configuration.ScoringAlgorithmConfiguration;
import fr.gouv.stopc.robert.server.batch.exception.RobertScoringException;
import fr.gouv.stopc.robert.server.batch.service.ScoringStrategyService;
import fr.gouv.stopc.robert.server.batch.vo.ScoringResult;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.HelloMessageDetail;
import lombok.extern.slf4j.Slf4j;

/**
 * Scoring strategy that implements the algorithm version 2
 * 
 * @author plant-stopcovid
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "robert.scoring.algo-version", havingValue = "2")
public class ScoringStrategyV2ServiceImpl implements ScoringStrategyService {

	private final IServerConfigurationService serverConfigurationService;

	private final ScoringAlgorithmConfiguration configuration;

	/**
	 * Spring injection constructor
	 * 
	 * @param serverConfigurationService the
	 *                                   <code>IServerConfigurationService</code>
	 *                                   bean to inject
	 * @param configuration              the
	 *                                   <code>ScoringAlgorithmConfiguration</code>
	 *                                   bean to inject
	 */
	public ScoringStrategyV2ServiceImpl(IServerConfigurationService serverConfigurationService,
			ScoringAlgorithmConfiguration configuration) {
		this.serverConfigurationService = serverConfigurationService;
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScoringResult execute(Contact contact) throws RobertScoringException {

		final int epochDurationInMinutes = serverConfigurationService.getEpochDurationSecs() / 60;

		// Variables
		final List<Number>[] listRSSI = new ArrayList[epochDurationInMinutes];
		double[] qm = new double[epochDurationInMinutes];
		int[] np = new int[epochDurationInMinutes];
		for (int k = 0; k < epochDurationInMinutes; k++) {
			listRSSI[k] = new ArrayList<Number>();
		}

		// Verification
		List<HelloMessageDetail> messageDetails = contact.getMessageDetails();
		if (messageDetails.size() == 0) {
			String errorMessage = "Cannot score contact with no HELLO messages";
			log.error(errorMessage);
			throw new RobertScoringException(errorMessage);
		}

		// Phase 1 : fading compensation
		// First index corresponds to the first minute of the first EBID emission
		double t0 = messageDetails.get(0).getTimeCollectedOnDevice();

		int vectorSize = messageDetails.size();
		for (int k = 0; k < vectorSize; k++) {
			HelloMessageDetail messageDetail = messageDetails.get(k);
			double timestampDelta = messageDetail.getTimeCollectedOnDevice() - t0;
			int index = (int) Math.floor(timestampDelta / 60.0);
			if ((index >= 0) && (index < epochDurationInMinutes)) {
				// Drop error Hello messages with too big RSSI (corresponding to errors in iOS
				// and Android)

				if (messageDetail.getRssiCalibrated() <= -5) {
					// Cutting peaks
					int rssi = Math.min(messageDetail.getRssiCalibrated(), configuration.getRssiMax());
					listRSSI[index].add(rssi);
				}
			} else {
				String errorMessage = "Epoch in minutes too big " + index;
				log.error(errorMessage);
				throw new RobertScoringException(errorMessage);
			}

		}

		// Phase 2: Average RSSI
		for (int k = 0; k < epochDurationInMinutes - 1; k++) {
			ArrayList<Number> listRssi2 = new ArrayList<>(listRSSI[k]);
			listRssi2.addAll(listRSSI[k + 1]);
			qm[k] = softMax(listRssi2, configuration.getSoftMaxA());
			np[k] = listRSSI[k].size() + listRSSI[k + 1].size();
		}
		// Only one window for the last sample
		qm[epochDurationInMinutes - 1] = softMax(listRSSI[epochDurationInMinutes - 1], configuration.getSoftMaxA());
		np[epochDurationInMinutes - 1] = listRSSI[epochDurationInMinutes - 1].size();

		// Phase 3: Risk scoring
		// https://hal.inria.fr/hal-02641630/document - eq (52)

		int kmax = 0;
		int nbcontacts = 0;
		List<Number> risk = new ArrayList<Number>();

		for (int k = 0; k < epochDurationInMinutes; k++) {
			if (np[k] > 0) {
				kmax = k;
				int dd = Math.min(np[k], configuration.getDeltas().length - 1);
				double gamma = (qm[k] - configuration.getP0()) / configuration.getDeltas()[dd];
				double vrisk = (gamma <= 0.0) ? 0.0 : (gamma >= 1) ? 1.0 : gamma;
				if (vrisk > 0) {
					nbcontacts++;
				}
				risk.add(vrisk);
			}
		}

		return ScoringResult.builder().rssiScore(softMax(risk, configuration.getSoftMaxB()))
				.duration(kmax).nbcontacts(nbcontacts).build();

	}

	/**
	 * 
	 * @param listValues
	 * @param softmaxCoef
	 * @return
	 */
	private Double softMax(List<Number> listValues, double softmaxCoef) {
		int ll = listValues.size();
		double vm = 0.0;

		if (ll > 0) {
			double vlog = 0.0;
			for (int i = 0; i < ll; i++) {
				vlog += Math.exp(listValues.get(i).doubleValue() / softmaxCoef);
			}
			vm = softmaxCoef * Math.log(vlog / ll);
		}
		return vm;
	}
}
