package fr.gouv.stopc.robertserver.ws.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.gouv.stopc.robertserver.ws.service.IKpiService;
import fr.gouv.stopc.robertserver.ws.vo.RobertServerKpi;

@Service
public class KpiServiceImpl implements IKpiService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RobertServerKpi> computeKpi(LocalDate fromDate, LocalDate toDate) {
		// TODO Auto-generated method stub
		return null;
	}

}
