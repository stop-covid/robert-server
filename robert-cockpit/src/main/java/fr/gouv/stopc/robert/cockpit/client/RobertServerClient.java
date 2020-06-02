package fr.gouv.stopc.robert.cockpit.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;

/**
 * OpenFeign client of the Robert Server API
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@FeignClient(name = "rs-client", url = "${robert.rs.api-url}", fallback = RobertServerClientFallback.class)
public interface RobertServerClient {

	/**
	 * Generates a list of Kpi on a period
	 * 
	 * @param parameters the query parameters of the Kpi generation method
	 * @return the list of dated Kpi produced by Robert Server
	 * @since 0.0.1-SNAPSHOT
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/kpi")
	public List<RobertServerKpi> getKpi(@RequestParam("fromDate") LocalDate fromDate,
			@RequestParam("toDate") LocalDate toDate);

}
