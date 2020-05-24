package fr.gouv.stopc.robert.config.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import fr.gouv.stopc.robert.config.dao.IRobertConfigurationDao;
import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.util.IConfigurationUpdateResults;
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

	/**
	 * A Yaml encoder/decoder
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Yaml yamlCodec;

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
	 * The configuration file name pattern
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final static String configurationFileNamePattern = "%s-%s.yml";

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
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setPrettyFlow(true);
			yamlCodec = new Yaml(options);
		} catch (IOException | URISyntaxException e) {
			log.error("Failed to connect to the Git repository : ", e);
		}
		Assert.notNull(gitWrapper, "Git connection must be established");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConfigurationHistoryEntry> getHistory(String appName, String profile) {
		try {
			Iterable<RevCommit> entries = gitWrapper.log().call();
			return StreamSupport.stream(entries.spliterator(), false)
					.filter(entry -> entry.getFullMessage().startsWith("[" + appName + "-" + profile))
					.map(entry -> mapper.toHistoryEntry(entry)).collect(Collectors.toList());
		} catch (GitAPIException e) {
			log.error("Failed to get history : ", e);
		}
		return new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateConfiguration(String appName, String profile, Map<String, Object> newConfiguration) {
		String result = IConfigurationUpdateResults.CONFIGURATION_UPDATED;
		try {
			// Compute the configuration file name to read
			String confFileName = String.format(configurationFileNamePattern, appName, profile);
			File currentFile = new File(gitWrapper.getRepository().getWorkTree(), confFileName);
			// Read the configuration from the file
			Map<String, Object> currentConfiguration = yamlCodec.load(new FileInputStream(currentFile));

			// Compute the commit message
			String commitMessage = computeCommitMessage(appName, profile, currentConfiguration, newConfiguration);

			if (commitMessage != null) {
				// If commit message is filled that means the configuration has changes
				FileUtils.writeByteArrayToFile(currentFile, yamlCodec.dump(newConfiguration).getBytes());
				gitWrapper.add().addFilepattern(".").call();
				gitWrapper.commit().setMessage(commitMessage).call();
			} else {
				// If commit message is notr filled that means the configuration has no change
				result = IConfigurationUpdateResults.NOTHING_TO_UPDATE;
			}

		} catch (GitAPIException | IOException e) {
			log.error("Error while updating " + appName + "configuration", e);
			result = IConfigurationUpdateResults.CONFIGURATION_UPDATE_FAILED;
		}
		return result;
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
	private String computeCommitMessage(String appName, String profile, Map<String, Object> currentConfiguration,
			Map<String, Object> newConfiguration) {
		StringBuffer commitMessage = new StringBuffer("[").append(appName).append("-").append(profile)
				.append(" configuration update]");
		boolean isModified = false;
		Map<String, String> newConfFlattened = flatten(newConfiguration);
		Map<String, String> currentConfFlattened = flatten(currentConfiguration);
		// Read the current configuration and look for new value for each current
		// configuration key in the new configuration
		for (Entry<String, String> currentProperty : currentConfFlattened.entrySet()) {
			if (!currentProperty.getValue().equals(newConfFlattened.get(currentProperty.getKey()))) {
				commitMessage.append("\n- ").append(currentProperty.getKey());
				commitMessage.append("\nOld value : ").append(currentProperty.getValue());
				commitMessage.append("\nNew value : ").append(newConfFlattened.get(currentProperty.getKey()));
				isModified = true;
			}
		}

		// If the configuration is not really updated then return a null message
		if (!isModified) {
			return null;
		}
		return commitMessage.toString();
	}

	/**
	 * Flatten the Yaml configuration in Map<String, Object> into Map<String,
	 * String>
	 * 
	 * @param config the configuration to flatten
	 * @return the flattened configuration
	 * @since 0.0.1-SNAPSHOT
	 */
	private Map<String, String> flatten(Map<String, Object> config) {

		Map<String, String> flattened = new HashMap<>();
		for (Entry<String, Object> entry : config.entrySet()) {
			String[] propertyFormatKey = flatten(entry.getKey(), entry.getValue()).split("\n");
			for (String aKey : propertyFormatKey) {
				String key = aKey.substring(0, aKey.indexOf("="));
				String value = aKey.substring(aKey.indexOf("=") + 1);
				flattened.put(key, value);
			}
		}

		return flattened;
	}

	/**
	 * Flatten in properties format
	 * 
	 * @param key  the parent key
	 * @param mapr the subkey / value
	 * @return the entry in properties format
	 * @since 0.0.1-SNAPSHOT
	 */
	@SuppressWarnings("unchecked")
	private String flatten(String key, Object mapr) {

		StringBuilder sb = new StringBuilder();

		if (!(mapr instanceof Map)) {
			sb.append(key + "=" + mapr + "\n");
			return sb.toString();
		}

		Map<String, Object> map = (Map<String, Object>) mapr;

		for (String mapKey : map.keySet()) {

			if (map.get(mapKey) instanceof Map) {
				sb.append(flatten(key + "." + mapKey, map.get(mapKey)));
			} else {
				sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
			}
		}

		return sb.toString();
	}
}