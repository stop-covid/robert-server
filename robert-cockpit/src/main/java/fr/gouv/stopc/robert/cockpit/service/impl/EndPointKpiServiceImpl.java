package fr.gouv.stopc.robert.cockpit.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;
import fr.gouv.stopc.robert.cockpit.service.IEndPointKpiService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the Kpi generation for endpoints service using
 * Elasticsearch queries on the supervision server
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
public class EndPointKpiServiceImpl implements IEndPointKpiService {

	/**
	 * Url of the Elasticsearch API
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@Value("${robert.supervision.elasticsearch-api-url}")
	private String supervisionEsUrl;

	/**
	 * Elasticsearch REST Client
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private RestHighLevelClient esRestClient;

	/**
	 * Initializes the Elasticsearch REST client
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@PostConstruct
	private void initEsClient() {
		esRestClient = RestClients.create(ClientConfiguration.builder().connectedTo(supervisionEsUrl).build()).rest();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EndPointKpi> getKpi(LocalDate fromDate, LocalDate toDate) {
		try {
			if (esRestClient.ping(RequestOptions.DEFAULT)) {
				// TODO modifier avec la requete de Tony
				MultiSearchResponse response = esRestClient.msearch(new MultiSearchRequest(),
						RequestOptions.DEFAULT.toBuilder().build());
				// TODO gérer la date et la map
				return Stream.of(response.getResponses())
						.map(x -> new EndPointKpi(null, x.getResponse().getAggregations().asMap()))
						.collect(Collectors.toList());
			}
		} catch (IOException e) {
			log.error("Failed to query Elasticsearch");
		}
		return new ArrayList<>();
	}

}
