package fr.gouv.stopc.robert.cockpit.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;

/**
 * Fallback class to use when Robert Server is not responding
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Component
public class RobertServerClientFallback implements RobertServerClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RobertServerKpi> getKpi(LocalDate fromDate, LocalDate toDate) {
		return new ArrayList<>();
	}

}