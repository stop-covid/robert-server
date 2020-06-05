package test.fr.gouv.stopc.robertserver.database.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;

import fr.gouv.stopc.robertserver.database.RobertServerDatabaseApplication;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.repository.RegistrationRepository;
import test.fr.gouv.stopc.robertserver.database.utils.RegistrationFactory;

@ContextConfiguration(classes = { RobertServerDatabaseApplication.class })
@DataMongoTest
public class RegistrationRepositoryTest {

	@Autowired
	private RegistrationRepository registrationRepository;

	@Test
	public void testSave() {
		SecureRandom sr = new SecureRandom();
		byte[] rndBytes = new byte[5];
		sr.nextBytes(rndBytes);

		// when
		Registration idTableTest = this.registrationRepository
				.insert(Registration.builder().permanentIdentifier(rndBytes).build());

		// Then
		assertNotNull(idTableTest);
	}

	@Test
	public void testGetFind() {

		// Given
		SecureRandom sr = new SecureRandom();
		byte[] rndBytes = new byte[5];
		sr.nextBytes(rndBytes);

		this.registrationRepository.insert(Registration.builder().permanentIdentifier(rndBytes).build());

		// When && Then
		assertTrue(this.registrationRepository.existsById(rndBytes));
	}

	@Test
	public void testCountNbAlertedUsers() {
		// 1st day [0; 95] : 1 not notified & 2 notified
		// 2nd day [96; 191] : 1 not notified & 2 notified
		// 3rd day [192; 287] : 1 not notified & 2 notified
		// 4th day [288; 383] : 0 registration
		// Total : 9 registration.
		// Expected : 
		// - beginning of 2nd day [epoch 96] -> 2 notified
		// - beginning of 3rd day [epoch 192] -> 4 notified
		// - beginning of 4th day [epoch 288] -> 6 notified
		List<Integer> creationEpochs = Arrays.asList(0, 96, 192);
		List<Integer> computationEpochs = Arrays.asList(96, 192, 288);
		// Purge the collection
		this.registrationRepository.deleteAll();
		// Save exposed users but not notified
		this.registrationRepository
				.insert(RegistrationFactory.generateAtRiskButNotNotifiedRegistration(creationEpochs, 1)).size();
		// Save exposed users and notified
		this.registrationRepository.insert(RegistrationFactory.generateNotifiedRegistration(creationEpochs, 2)).size();
		this.registrationRepository.saveAll(RegistrationFactory.generateNotifiedRegistration(creationEpochs, 3)).size();
		// Compute the kpis
		List<Long> kpiNbNotified = computationEpochs.stream()
				.map(epoch -> this.registrationRepository.countNbAlertedUsers(epoch)).collect(Collectors.toList());
		assertEquals(3, kpiNbNotified.size());
		assertEquals(Arrays.asList(2L, 4L, 6L), kpiNbNotified);
	}
	
	@Test
	public void testCountNbExposedUsersButNotAtRisk() {
		// 1st day [0; 95] : 1 not notified & 1 notified & 1 exposed but not at risk
		// 2nd day [96; 191] : 1 not notified & 1 notified & 1 exposed but not at risk
		// 3rd day [192; 287] : 1 not notified & 1 notified & 1 exposed but not at risk
		// 4th day [288; 383] : 0 registration
		// Total : 9 registration.
		// Expected : 
		// - beginning of 2nd day [epoch 96] -> 1 notified
		// - beginning of 3rd day [epoch 192] -> 2 notified
		// - beginning of 4th day [epoch 288] -> 3 notified
		List<Integer> creationEpochs = Arrays.asList(0, 96, 192);
		List<Integer> computationEpochs = Arrays.asList(96, 192, 288);
		// Purge the collection
		this.registrationRepository.deleteAll();
		// Save exposed users but not notified
		this.registrationRepository
				.insert(RegistrationFactory.generateExposedButNotAtRiskRegistration(creationEpochs, 1)).size();
		// Save exposed users and notified
		this.registrationRepository.insert(RegistrationFactory.generateNotifiedRegistration(creationEpochs, 2)).size();
		this.registrationRepository.insert(RegistrationFactory.generateAtRiskButNotNotifiedRegistration(creationEpochs, 3)).size();
		// Compute the kpis
		List<Long> kpiNbNotified = computationEpochs.stream()
				.map(epoch -> this.registrationRepository.countNbExposedUsersButNotAtRisk(epoch)).collect(Collectors.toList());
		assertEquals(3, kpiNbNotified.size());
		assertEquals(Arrays.asList(1L, 2L, 3L), kpiNbNotified);
	}
}
