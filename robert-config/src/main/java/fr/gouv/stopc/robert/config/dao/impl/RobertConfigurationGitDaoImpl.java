package fr.gouv.stopc.robert.config.dao.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.gouv.stopc.robert.config.dao.IRobertConfigurationDao;
import fr.gouv.stopc.robert.config.dto.ComparisonResult;
import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;
import fr.gouv.stopc.robert.config.util.RobertConfigMapper;
import fr.gouv.stopc.robert.config.util.RobertConfigurationServerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the configuration DAO using a GIT backend
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Repository
@Profile("git")
public class RobertConfigurationGitDaoImpl implements IRobertConfigurationDao {

	/**
	 * Git Wrapper to manipulate the configuration repo
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Git gitWrapper;

	private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

	/**
	 * 
	 */
	private JavaPropsMapper propsMapper = new JavaPropsMapper();

	/**
	 * The DTO mapper
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final RobertConfigMapper mapper;

	/**
	 * The configuration of the config server
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final RobertConfigurationServerConfig config;

	/**
	 * Spring Injection constructor
	 * 
	 * @param config
	 * @param mapper
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertConfigurationGitDaoImpl(RobertConfigurationServerConfig config, RobertConfigMapper mapper) {
		this.mapper = mapper;
		this.config = config;
	}

	/**
	 * Initializes the GIT connection. The GIT wrapper must not be null
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@PostConstruct
	public void initGitConnection() {
		try {
			gitWrapper = Git.open(Paths.get(new URL(config.getGitUri()).toURI()).toFile());
			log.info("Connected to the Git repository");
		} catch (IOException | URISyntaxException e) {
			log.error("Failed to connect to the Git repository : ", e);
		}
		Assert.notNull(gitWrapper, "Git connection must be established");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConfigurationHistoryEntry> getHistory(String profile) {
		try {
			if (!gitWrapper.getRepository().getBranch().equals(profile)) {
				gitWrapper.checkout().setName(profile).call();
			}
			Iterable<RevCommit> entries = gitWrapper.log().call();
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
			if (!gitWrapper.getRepository().getBranch().equals(profile)) {
				gitWrapper.checkout().setName(profile).call();
			}
			// Transform the new configuration object into a Map<String, String>
			Properties newConf = propsMapper.writeValueAsProperties(newConfiguration);
			// Store for each configuration file the comparison results. TreeSet is used to
			// avoid duplicated entries
			Set<ComparisonResult> comparisonResults = new TreeSet<>();
			for (File confFile : this.gitWrapper.getRepository().getWorkTree().listFiles(x -> x.isFile())) {
				// Transform the current configuration object into a Map<String, String>
				FunctionalConfiguration confFileContent = yamlMapper.readValue(confFile, FunctionalConfiguration.class);
				// signalCalibrationPerModel and delta are list so we have to handle them
				// manually
				List<ComparisonResult> results = new ArrayList<>();
				results.addAll(updateListParameters(confFileContent, newConfiguration));
				Properties currentConf = propsMapper.writeValueAsProperties(confFileContent);
				results.addAll(compareAndUpdateConfigurations(currentConf, newConf));
				if (!CollectionUtils.isEmpty(results)) {
					confFileContent = propsMapper.readPropertiesAs(currentConf, FunctionalConfiguration.class);
					// The configuration need to be updated, add the application name to the list
					result.add(confFile.getName().substring(0, confFile.getName().lastIndexOf("-")));
					// Write the new configuration
					comparisonResults.addAll(results);
					yamlMapper.writeValue(confFile, confFileContent);
				}

			}

			// Compute the commit message and commit the updated
			if (!CollectionUtils.isEmpty(comparisonResults)) {
				String message = computeCommitMessage(profile, comparisonResults);
				gitWrapper.add().addFilepattern(".").call();
				gitWrapper.commit().setMessage(message).call();
			}
		} catch (GitAPIException | IOException e) {
			log.error("Error updating the configuration");
			result = null;
		}
		return result;
	}

	/**
	 * 
	 * @param confFileContent
	 * @param newConfiguration
	 * @return
	 */
	private List<ComparisonResult> updateListParameters(FunctionalConfiguration confFileContent,
			FunctionalConfiguration newConfiguration) {

		List<ComparisonResult> result = new ArrayList<>();
		if (confFileContent.getProximityTracing() != null && confFileContent.getProximityTracing().getBle() != null) {
			// Update signalCalibrationPerModel if present in the current conf
			if (!confFileContent.getProximityTracing().getBle().getSignalCalibrationPerModel()
					.equals(newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel())) {
				ComparisonResult signalCalibrationCompare = new ComparisonResult();
				signalCalibrationCompare.setKey("proximityTracing.ble.signalCalibrationPerModel");
				signalCalibrationCompare.setCurrentValue(
						confFileContent.getProximityTracing().getBle().getSignalCalibrationPerModel().toString());
				signalCalibrationCompare.setNewValue(
						newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel().toString());
				result.add(signalCalibrationCompare);
				confFileContent.getProximityTracing().getBle().setSignalCalibrationPerModel(
						newConfiguration.getProximityTracing().getBle().getSignalCalibrationPerModel());
			}
			// Update delta if present in the current conf
			if (!confFileContent.getProximityTracing().getBle().getDelta()
					.equals(newConfiguration.getProximityTracing().getBle().getDelta())) {
				ComparisonResult signalCalibrationCompare = new ComparisonResult();
				signalCalibrationCompare.setKey("proximityTracing.ble.delta");
				signalCalibrationCompare
						.setCurrentValue(confFileContent.getProximityTracing().getBle().getDelta().toString());
				signalCalibrationCompare
						.setNewValue(newConfiguration.getProximityTracing().getBle().getDelta().toString());
				result.add(signalCalibrationCompare);
				confFileContent.getProximityTracing().getBle()
						.setDelta(newConfiguration.getProximityTracing().getBle().getDelta());
			}
		}

		return result;
	}

	/**
	 * 
	 * @param profile
	 * @param results
	 * @return
	 */
	public String computeCommitMessage(String profile, Set<ComparisonResult> results) {
		StringBuffer sb = new StringBuffer("[Configuration update]");
		for (ComparisonResult result : results) {
			sb.append("\n-").append(result.getKey());
			sb.append("\n Old value : ").append(result.getCurrentValue());
			sb.append("\n New value : ").append(result.getNewValue());
		}
		return sb.toString();
	}

	/**
	 * Function computing the commit message
	 * 
	 * @param appName              the name of the application
	 * @param profile              the profil on which the application is running
	 * @param currentConfiguration the current configuration under GIT
	 * @param newConfiguration     the new configuration to take into account
	 * @return the commit message if there are modifications, else null
	 * @since 0.0.1-SNAPSHOT
	 */
	private List<ComparisonResult> compareAndUpdateConfigurations(Properties currentConfiguration,
			Properties newConfiguration) {
		List<ComparisonResult> result = new ArrayList<>();
		for (Entry<Object, Object> newConfEntry : newConfiguration.entrySet()) {
			if (currentConfiguration.containsKey(newConfEntry.getKey())
					&& !newConfEntry.getValue().equals(currentConfiguration.get(newConfEntry.getKey()))) {
				// Values are different -> create a new result
				result.add(new ComparisonResult((String) newConfEntry.getKey(),
						String.valueOf(currentConfiguration.get(newConfEntry.getKey())),
						String.valueOf(newConfEntry.getValue())));
				// Update the current configuration
				currentConfiguration.put(newConfEntry.getKey(), newConfEntry.getValue());
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FunctionalConfiguration getConfiguration(String profile) {
		Properties agregatedProps = new Properties();

		FunctionalConfiguration result;
		try {
			if (!gitWrapper.getRepository().getBranch().equals(profile)) {
				gitWrapper.checkout().setName(profile).call();
			}
			for (File confFile : this.gitWrapper.getRepository().getWorkTree().listFiles(x -> x.isFile())) {
				// Transform the current configuration object into a Map<String, String>
				FunctionalConfiguration yamlConf = yamlMapper.readValue(confFile, FunctionalConfiguration.class);
				Properties currentConf = propsMapper.writeValueAsProperties(yamlConf);
				currentConf.entrySet().forEach(entry -> agregatedProps.put(entry.getKey(), entry.getValue()));
			}
			result = propsMapper.readPropertiesAs(agregatedProps, FunctionalConfiguration.class);
		} catch (GitAPIException | IOException e) {
			log.error("Error retrieving the configuration");
			result = null;
		}
		return result;
	}
}