package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDate;
import java.util.Map;

import javax.validation.constraints.NotNull;

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
	@NotNull
	private LocalDate date;

	/**
	 * Map of the results of the elasticsearch queries
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Map<String, Aggregation> aggregationsMap;

	/**
	 * Constructor
	 * 
	 * @param aggregationsMap the aggregation map to use
	 */
	public EndPointKpi(LocalDate date, Map<String, Aggregation> aggregationsMap) {
		this.date = date;
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
	public Long getStatusEndpointCalls() {
		// TODO use the aggregationsMap
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public Long getDeleteHistoryEndpointCalls() {
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
