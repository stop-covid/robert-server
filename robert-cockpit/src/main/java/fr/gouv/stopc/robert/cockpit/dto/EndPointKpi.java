package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDateTime;
import java.util.Map;

import org.elasticsearch.search.aggregations.Aggregation;

import lombok.Getter;

/**
 * Kpis of endpoints usage
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public class EndPointKpi {

	@Getter
	private LocalDateTime date;

	/**
	 * Map of the results of the elasticsearch queries
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Map<String, Aggregation> aggregationsMap;

	/**
	 * Constructor
	 * 
	 * @param aggregationsMap
	 */
	public EndPointKpi(Map<String, Aggregation> aggregationsMap) {
		this.aggregationsMap = aggregationsMap;
	}

	/**
	 * 
	 * @return
	 */
	public Long getReportEndpointCalls() {
		// TODO use the aggregationsMap
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public Long getRegisterEndpointCalls() {
		// TODO use the aggregationsMap
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public Long getUnregisterEndpointCalls() {
		// TODO use the aggregationsMap
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public Long getVerifyEndpointCalls() {
		// TODO use the aggregationsMap
		return null;
	}
}
