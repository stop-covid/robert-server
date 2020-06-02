package fr.gouv.stopc.robert.cockpit.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.cockpit.client.RobertServerClient;
import fr.gouv.stopc.robert.cockpit.client.SubmissionCodeServerClient;
import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;
import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;
import fr.gouv.stopc.robert.cockpit.dto.StopCovidKpi;
import fr.gouv.stopc.robert.cockpit.dto.SubmissionCodeServerKpi;
import fr.gouv.stopc.robert.cockpit.service.IEndPointKpiService;
import fr.gouv.stopc.robert.cockpit.service.IStopCovidKpiGenerationService;

/**
 * Kpi generation service
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Service
public class StopCovidKpiGenerationServiceImpl implements IStopCovidKpiGenerationService {

	/**
	 * Url of the API of Robert Server
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@Value("${robert.rs.api-url}")
	private String robertServerApiUrl;

	/**
	 * Url of the API of Submission Code Server
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@Value("${robert.scs.api-url}")
	private String submissionCodeServerApiUrl;

	/**
	 * Robert Server OpenFeign client
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private RobertServerClient rsClient;

	/**
	 * Submission Code Server OpenFeign client
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private SubmissionCodeServerClient scsClient;

	/**
	 * Endpoint Kpi retrieval service
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private IEndPointKpiService endpointKpiService;

	/**
	 * Spring Injection Constructor
	 * 
	 * @param endpointKpiService the endpoint Kpi retrieval service instance to use
	 * @since 0.0.1-SNAPSHOT
	 */
	public StopCovidKpiGenerationServiceImpl(IEndPointKpiService endpointKpiService, RobertServerClient rsClient,
			SubmissionCodeServerClient scsClient) {
		this.endpointKpiService = endpointKpiService;
		this.rsClient = rsClient;
		this.scsClient = scsClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StopCovidKpi> computeKpis(LocalDate fromDate, LocalDate toDate) {
		List<StopCovidKpi> result = new ArrayList<StopCovidKpi>();

		// Retrieve the different Kpis (Robert, SCS and Supervision)
		List<RobertServerKpi> rsKpis = rsClient.getKpi(fromDate, toDate);
		List<EndPointKpi> epKpis = endpointKpiService.getKpi(fromDate, toDate);
		List<SubmissionCodeServerKpi> scsKpis = scsClient.getKpi(fromDate, toDate);

		// Convert into maps (easier to use)
		Map<LocalDate, RobertServerKpi> rsKpiMap = rsKpis.stream()
				.collect(Collectors.toMap(RobertServerKpi::getDate, Function.identity()));

		Map<LocalDate, SubmissionCodeServerKpi> scsKpiMap = scsKpis.stream()
				.collect(Collectors.toMap(SubmissionCodeServerKpi::getDate, Function.identity()));

		Map<LocalDate, EndPointKpi> epKpiMap = epKpis.stream()
				.collect(Collectors.toMap(EndPointKpi::getDate, Function.identity()));

		// Convert the three lists of Kpi into a single one
		if (rsKpiMap.size() >= scsKpiMap.size() && rsKpiMap.size() >= epKpiMap.size()) {
			// Case of Robert Server Kpis number is the greatest of three sources
			// Handle the case of the three number of kpis are equal
			result = rsKpiMap.entrySet().stream()
					.map(x -> new StopCovidKpi(x.getValue(), scsKpiMap.get(x.getKey()), epKpiMap.get(x.getKey())))
					.collect(Collectors.toList());
		} else if (scsKpiMap.size() >= rsKpiMap.size() && scsKpiMap.size() >= epKpiMap.size()) {
			// Case of Submission Code Server Kpis number is the greatest of three sources
			result = scsKpiMap.entrySet().stream()
					.map(x -> new StopCovidKpi(rsKpiMap.get(x.getKey()), x.getValue(), epKpiMap.get(x.getKey())))
					.collect(Collectors.toList());
		} else {
			// Case of Supervision Kpis number is the greatest of three sources
			result = epKpiMap.entrySet().stream()
					.map(x -> new StopCovidKpi(rsKpiMap.get(x.getKey()), scsKpiMap.get(x.getKey()), x.getValue()))
					.collect(Collectors.toList());
		}
		return result;
	}

}
