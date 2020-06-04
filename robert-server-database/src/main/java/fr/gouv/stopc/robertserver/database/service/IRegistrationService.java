package fr.gouv.stopc.robertserver.database.service;

import java.util.List;
import java.util.Optional;

import fr.gouv.stopc.robertserver.database.model.Registration;

/**
 *
 */
public interface IRegistrationService {

	/**
	 * 
	 * @param id
	 * @return
	 */
	Optional<Registration> createRegistration(byte[] id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	Optional<Registration> findById(byte[] id);

	/**
	 * 
	 * @param registration
	 * @return
	 */
	Optional<Registration> saveRegistration(Registration registration);

	/**
	 * 
	 * @param registration
	 */
	void delete(Registration registration);

	/**
	 * 
	 */
	void deleteAll();

	/**
	 * 
	 * @return
	 */
	List<Registration> findAll();

	/**
	 * Return the numbers of alerted users until an epoch number for a given list of
	 * epoch
	 * 
	 * @param epoch the epoch number upper bound list
	 * @return the numbers of alerted users until each given epoch number
	 */
	List<Long> countNbAlertedUsers(List<Integer> epoch);

	/**
	 * Return the number of exposed users who are not at risk until an epoch number
	 * 
	 * @param epoch the epoch number upper bound
	 * @return the number of exposed but not at risk users until each given epoch number
	 */
	List<Long> countNbExposedButNotAtRiskUsers(List<Integer> epoch);
}
