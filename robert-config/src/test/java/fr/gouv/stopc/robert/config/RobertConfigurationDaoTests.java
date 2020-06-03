package fr.gouv.stopc.robert.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;

import fr.gouv.stopc.robert.config.dao.IRobertConfigurationDao;

@SpringBootTest
class RobertConfigurationDaoTests {
	
	@Autowired
	private IRobertConfigurationDao configDao;
	
	private HttpEntity<String> updateRequest;
	
	@Test
	void contextLoads() {
	}

}
