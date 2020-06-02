package fr.gouv.stopc.robert.cockpit.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.gouv.stopc.robert.cockpit.dto.SubmissionCodeServerKpi;

/**
 * Fallback class to use when Submission Code Server is not responding
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Component
public class SubmissionCodeServerClientFallback implements SubmissionCodeServerClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SubmissionCodeServerKpi> getKpi(LocalDate fromDate, LocalDate toDate) {
		return new ArrayList<>();
	}

}