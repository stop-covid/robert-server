package fr.gouv.stopc.robert.config.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import fr.gouv.stopc.robert.config.dao.IRobertConfigurationDao;
import fr.gouv.stopc.robert.config.dto.ComparisonResult;
import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;
import fr.gouv.stopc.robert.config.util.RobertConfigMapper;
import fr.gouv.stopc.robert.config.util.RobertConfigRepoWrapper;
import fr.gouv.stopc.robert.config.util.RobertConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the configuration DAO using a GIT backend
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Repository
public class RobertConfigurationGitDaoImpl implements IRobertConfigurationDao {

	/**
	 * The DTO mapper
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final RobertConfigMapper mapper;

	/**
	 * The repo wrapper
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final RobertConfigRepoWrapper repoWrapper;

	/**
	 * Spring Injection constructor
	 * 
	 * @param mapper the DTO mapper to use
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertConfigurationGitDaoImpl(RobertConfigMapper mapper, RobertConfigRepoWrapper repoWrapper) {
		this.mapper = mapper;
		this.repoWrapper = repoWrapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConfigurationHistoryEntry> getHistory(String profile) {
		try {
			Iterable<RevCommit> entries = repoWrapper.getHistory(profile);
			return StreamSupport.stream(entries.spliterator(), false)
					.filter(entry -> entry.getFullMessage().startsWith("[Configuration update]"))
					.map(entry -> mapper.toHistoryEntry(entry)).collect(Collectors.toList());
		} catch (GitAPIException | IOException e) {
			log.error("Failed to get history : ", e);
		}
		return new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> updateConfiguration(String profile, FunctionalConfiguration newConfiguration) {
		List<String> result = new ArrayList<>();
		try {
			// Checkout on the branch associated to the profile
			repoWrapper.switchTo(profile);
			// Transform the new configuration object into a Map<String, String>
			Properties newConf = mapper.toProperties(newConfiguration);
			// Store for each configuration file the comparison results. TreeSet is used to
			// avoid duplicated entries (a parameter can be stored in multiple files)
			Set<ComparisonResult> comparisonResults = new TreeSet<>();
			for (File confFile : repoWrapper.listConfigurationFiles(profile)) {
				// Transform the current configuration object into a Map<String, String>
				FunctionalConfiguration confFileContent = mapper.toFunctionalConfiguration(confFile);
				// signalCalibrationPerModel and delta are list so we have to handle them
				// manually
				List<ComparisonResult> results = new ArrayList<>();
				// Updating parameters that are stored as list (properties are not efficient to
				// handle lists)
				results.addAll(updateListParameters(confFileContent, newConfiguration));
				// As list parameters are equal now compare all other parameters
				Properties currentConf = mapper.toProperties(confFileContent);
				results.addAll(RobertConfigUtils.compareAndUpdateConfigurations(currentConf, newConf));
				if (!CollectionUtils.isEmpty(results)) {
					// If there are differences between current & new configuration then update the
					// current configuration
					RobertConfigUtils.removeEmptyProperties(currentConf);
					confFileContent = mapper.toFunctionalConfiguration(currentConf);
					// The configuration need to be updated, add the application name to the list
					result.add(confFile.getName().substring(0, confFile.getName().lastIndexOf("-")));
					comparisonResults.addAll(results);
					// Write the new configuration
					mapper.toYamlFile(confFile, confFileContent);
				}

			}

			if (!CollectionUtils.isEmpty(comparisonResults)) {
				// If any change, compute the commit message and commit the updated
				// configuration files
				String message = RobertConfigUtils.computeCommitMessage(comparisonResults);
				repoWrapper.saveConfiguration(profile, message);
			}
		} catch (GitAPIException | IOException e) {
			log.error("Error updating the configuration");
			result = null;
		}
		return result;
	}

	/**
	 * Function used to update parameters stored as lists. If there are differences
	 * -> new values list is the reference
	 * 
	 * @param currentConfiguration current configuration (from Git)
	 * @param newConfiguration     new configuration (from GUI)
	 * @return the comparison results for each parameters stored as lists
	 * @since 0.0.1-SNAPSHOT
	 */
	private List<ComparisonResult> updateListParameters(FunctionalConfiguration currentConfiguration,
			FunctionalConfiguration newConfiguration) {

		List<ComparisonResult> result = new ArrayList<>();
		// Configuration can be null if conf file is empty
		if (currentConfiguration != null && currentConfiguration.getProximityTracing() != null
				&& currentConfiguration.getProximityTracing().getBle() != null) {
			// Update signalCalibrationPerModel if present in the current conf
			if (!currentConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel()
					.equals(newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel())) {
				ComparisonResult signalCalibrationCompare = new ComparisonResult();
				signalCalibrationCompare.setKey("proximityTracing.ble.signalCalibrationPerModel");
				signalCalibrationCompare.setCurrentValue(
						currentConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel().toString());
				signalCalibrationCompare.setNewValue(
						newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel().toString());
				result.add(signalCalibrationCompare);
				currentConfiguration.getProximityTracing().getBle().setSignalCalibrationPerModel(
						newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel());
			}
			// Update delta if present in the current conf
			if (!currentConfiguration.getProximityTracing().getBle().getDelta()
					.equals(newConfiguration.getProximityTracing().getBle().getDelta())) {
				ComparisonResult signalCalibrationCompare = new ComparisonResult();
				signalCalibrationCompare.setKey("proximityTracing.ble.delta");
				signalCalibrationCompare
						.setCurrentValue(currentConfiguration.getProximityTracing().getBle().getDelta().toString());
				signalCalibrationCompare
						.setNewValue(newConfiguration.getProximityTracing().getBle().getDelta().toString());
				result.add(signalCalibrationCompare);
				currentConfiguration.getProximityTracing().getBle()
						.setDelta(newConfiguration.getProximityTracing().getBle().getDelta());
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FunctionalConfiguration getConfiguration(String profile) {
		// Properties used to aggregate configuration spread over files
		Properties aggregatedProps = new Properties();

		FunctionalConfiguration result;
		try {
			for (File confFile : repoWrapper.listConfigurationFiles(profile)) {
				// Transform the current configuration into object then into a Properties. It's
				// easier to aggregate properties values as the configuration is spread over
				// several files
				FunctionalConfiguration yamlConf = mapper.toFunctionalConfiguration(confFile);
				if (yamlConf != null) {
					// Can be null if file is empty
					Properties currentConf = mapper.toProperties(yamlConf);
					currentConf.entrySet().stream().filter(x -> !StringUtils.isEmpty(x.getValue()))
							.forEach(entry -> aggregatedProps.put(entry.getKey(), entry.getValue()));
				}
			}
			// When all files have been aggregated, transform the properties into java
			// object
			result = mapper.toFunctionalConfiguration(aggregatedProps);
		} catch (GitAPIException | IOException e) {
			log.error("Error retrieving the configuration", e);
			result = null;
		}
		return result;
	}

}