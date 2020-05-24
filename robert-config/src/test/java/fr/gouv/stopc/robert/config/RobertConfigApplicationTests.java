package fr.gouv.stopc.robert.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;

@SpringBootTest
class RobertConfigApplicationTests {
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	private HttpEntity<String> updateRequest;
	
	@Test
	void contextLoads() {
	}

}
