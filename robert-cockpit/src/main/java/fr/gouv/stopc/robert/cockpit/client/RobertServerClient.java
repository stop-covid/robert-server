package fr.gouv.stopc.robert.cockpit.client;

import java.util.List;
import java.util.Map;

import feign.QueryMap;
import feign.RequestLine;
import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;

/**
 * OpenFeign client of the Robert Server API
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface RobertServerClient {

	/**
	 * Generates a list of Kpi on a period
	 * 
	 * @param parameters the query parameters of the Kpi generation method
	 * @return the list of dated Kpi produced by Robert Server
	 * @since 0.0.1-SNAPSHOT
	 */
	@RequestLine("GET ${robert.rs.kpi-endpoint}?{parameters}")
	public List<RobertServerKpi> getKpi(@QueryMap Map<String, Object> parameters);

}
