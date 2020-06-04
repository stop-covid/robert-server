package fr.gouv.stopc.robertserver.database.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.repository.RegistrationRepository;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;

@Service
public class RegistrationService implements IRegistrationService {

	private RegistrationRepository registrationRepository;

	/**
	 * Spring injection constructor
	 * 
	 * @param registrationRepository the <code>RegistrationRepository</code> bean to
	 *                               inject
	 */
	public RegistrationService(RegistrationRepository registrationRepository) {
		this.registrationRepository = registrationRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Registration> createRegistration(byte[] id) {
		return Optional.ofNullable(Registration.builder().permanentIdentifier(id).build())
				.map(this.registrationRepository::insert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Registration> findById(byte[] id) {
		return this.registrationRepository.findById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Registration> saveRegistration(Registration registration) {
		return Optional.ofNullable(registration).map(this.registrationRepository::save);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Registration registration) {
		Optional.ofNullable(registration).ifPresent(this.registrationRepository::delete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteAll() {
		this.registrationRepository.deleteAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Registration> findAll() {
		return this.registrationRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> countNbAlertedUsers(List<Integer> epochs) {
		return epochs.stream().map(epoch -> this.registrationRepository.countNbAlertedUsers(epoch))
				.collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> countNbExposedButNotAtRiskUsers(List<Integer> epochs) {
		return epochs.stream().map(epoch -> this.registrationRepository.countNbExposedUsersButNotAtRisk(epoch))
				.collect(Collectors.toList());
	}

}
