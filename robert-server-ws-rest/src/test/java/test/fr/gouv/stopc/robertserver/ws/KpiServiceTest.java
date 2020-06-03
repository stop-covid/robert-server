package test.fr.gouv.stopc.robertserver.ws;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robertserver.database.repository.ContactRepository;
import fr.gouv.stopc.robertserver.database.repository.RegistrationRepository;
import fr.gouv.stopc.robertserver.ws.service.IKpiService;
import test.fr.gouv.stopc.robertserver.ws.utils.KpiFactory;

@ExtendWith(SpringExtension.class)
public class KpiServiceTest {

	@Autowired
	private IKpiService kpiService;

	@MockBean
	private ContactRepository contactRepository;

	@MockBean
	private RegistrationRepository registrationRepository;

	@Autowired
	private KpiFactory kpiFactory;

	@Test
	public void testComputeKpi() {
		fail("Not yet implemented");
	}

}
