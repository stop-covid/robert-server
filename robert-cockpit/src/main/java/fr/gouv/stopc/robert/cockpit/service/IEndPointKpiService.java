package fr.gouv.stopc.robert.cockpit.service;

import java.time.LocalDateTime;
import java.util.List;

import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;

/**
 * 
 * @author MROUANET
 *
 */
public interface IEndPointKpiService {

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public List<EndPointKpi> getKpi(LocalDateTime fromDate, LocalDateTime toDate);

}
