package fr.gouv.stopc.robert.cockpit.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import feign.Feign;
import fr.gouv.stopc.robert.cockpit.client.RobertServerClient;
import fr.gouv.stopc.robert.cockpit.client.SubmissionCodeServerClient;
import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;
import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;
import fr.gouv.stopc.robert.cockpit.dto.StopCovidKpi;
import fr.gouv.stopc.robert.cockpit.dto.SubmissionCodeServerKpi;
import fr.gouv.stopc.robert.cockpit.service.IEndPointKpiService;
import fr.gouv.stopc.robert.cockpit.service.IRobertKpiGenerationService;

/**
 * Kpi generation service
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Service
public class RobertKpiGenerationServiceImpl implements IRobertKpiGenerationService {

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
	public RobertKpiGenerationServiceImpl(IEndPointKpiService endpointKpiService) {
		this.endpointKpiService = endpointKpiService;
	}

	/**
	 * Initializes the OpenFeign clients
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@PostConstruct
	private void initFeignClients() {
		rsClient = Feign.builder().target(RobertServerClient.class, robertServerApiUrl);
		scsClient = Feign.builder().target(SubmissionCodeServerClient.class, submissionCodeServerApiUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StopCovidKpi> computeKpis(LocalDateTime fromDate, LocalDateTime toDate) {
		List<StopCovidKpi> result = new ArrayList<StopCovidKpi>();

		// Create the parameters map for the OpenFeign client calls
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("fromDate", fromDate);
		queryParameters.put("toDate", toDate);

		// Retrieve the different Kpis (Robert, SCS and Supervision)
		List<RobertServerKpi> rsKpis = rsClient.getKpi(queryParameters);
		List<EndPointKpi> epKpis = endpointKpiService.getKpi(fromDate, toDate);
		List<SubmissionCodeServerKpi> scsKpis = scsClient.getKpi(queryParameters);

		// Convert into maps (easier to use)
		Map<LocalDateTime, RobertServerKpi> rsKpiMap = rsKpis.stream()
				.collect(Collectors.toMap(RobertServerKpi::getDate, Function.identity()));

		Map<LocalDateTime, SubmissionCodeServerKpi> scsKpiMap = scsKpis.stream()
				.collect(Collectors.toMap(SubmissionCodeServerKpi::getDate, Function.identity()));

		Map<LocalDateTime, EndPointKpi> epKpiMap = epKpis.stream()
				.collect(Collectors.toMap(EndPointKpi::getDate, Function.identity()));

		// Convert the three lists of Kpi into a single one
		result = rsKpiMap.entrySet().stream()
				.map(x -> new StopCovidKpi(x.getValue(), scsKpiMap.get(x.getKey()), epKpiMap.get(x.getKey())))
				.collect(Collectors.toList());

		return result;
	}

}
