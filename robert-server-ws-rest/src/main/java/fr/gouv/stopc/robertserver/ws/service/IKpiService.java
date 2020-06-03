package fr.gouv.stopc.robertserver.ws.service;

import java.time.LocalDate;
import java.util.List;

import fr.gouv.stopc.robertserver.ws.vo.RobertServerKpi;

/**
 * 
 * @author plant-stopcovid
 *
 */
public interface IKpiService {

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<RobertServerKpi> computeKpi(LocalDate fromDate, LocalDate toDate);

}
