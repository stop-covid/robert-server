package test.fr.gouv.stopc.robertserver.ws.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.gouv.stopc.robertserver.database.service.impl.RegistrationService;
import fr.gouv.stopc.robertserver.ws.RobertServerWsRestApplication;
import fr.gouv.stopc.robertserver.ws.service.IKpiService;

@SpringBootTest(classes = RobertServerWsRestApplication.class)
public class KpiGenerationServiceTest {

	@Autowired
	private IKpiService serviceKpi;
	
	@Test
	public void givenDayRange_whenComputeKpis_thenReturnSingleKpi() {
		
	}
}
