package fr.gouv.stopc.robertserver.database.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fr.gouv.stopc.robertserver.database.model.Registration;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, byte[]> {

	/**
	 * 
	 * @param epochFrom
	 * @param epochTo
	 * @return
	 */
	@Query(value = "{ latestRiskEpoch: {$lt: ?0}, atRisk: {$eq: true} , isNotified: {$eq: true}}", count = true)
	Long countNbAlertedUsers(int epoch);

	/**
	 * 
	 * @param epochFrom
	 * @param epochTo
	 * @return
	 */
	@Query(value = "{ latestRiskEpoch: {$lt: ?0}, atRisk: {$eq: false} , exposedEpochs: {$ne: []}}", count = true)
	Long countNbExposedUsersButNotAtRisk(int epoch);
}
