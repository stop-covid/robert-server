package fr.gouv.stopc.robert.cockpit.client;

import java.util.List;
import java.util.Map;

import feign.QueryMap;
import feign.RequestLine;
import fr.gouv.stopc.robert.cockpit.dto.SubmissionCodeServerKpi;

/**
 * OpenFeign client of the Submission Code Server API
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface SubmissionCodeServerClient {

	/**
	 * Generates a list of Kpi on a period
	 * 
	 * @param parameters the query parameters of the Kpi generation method
	 * @return the list of dated Kpi produced by Submission Code Server
	 * @since 0.0.1-SNAPSHOT
	 */
	@RequestLine("GET ${robert.scs.kpi-endpoint}?{parameters}")
	public List<SubmissionCodeServerKpi> getKpi(@QueryMap Map<String, Object> parameter);

}
