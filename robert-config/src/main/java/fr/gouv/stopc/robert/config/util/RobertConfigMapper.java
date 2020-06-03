package fr.gouv.stopc.robert.config.util;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;

@Component
public class RobertConfigMapper {

	/**
	 * Mapper to load configuration files as java objects
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

	/**
	 * Mapper to load objects as properties
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private JavaPropsMapper propsMapper = new JavaPropsMapper();

	/**
	 * 
	 * @param gitCommit
	 * @return
	 */
	public ConfigurationHistoryEntry toHistoryEntry(RevCommit gitCommit) {
		Date refEntryDate = gitCommit.getAuthorIdent().getWhen();
		ZoneId zone = gitCommit.getAuthorIdent().getTimeZone().toZoneId();
		return new ConfigurationHistoryEntry(gitCommit.getFullMessage(),
				refEntryDate.toInstant().atZone(zone).toLocalDateTime());
	}

	/**
	 * 
	 * @param yamlConfFile
	 * @return
	 * @throws IOException
	 */
	public FunctionalConfiguration toFunctionalConfiguration(File yamlConfFile) throws IOException {
		return yamlMapper.readValue(yamlConfFile, FunctionalConfiguration.class);
	}

	/**
	 * 
	 * @param yamlConfFile
	 * @return
	 * @throws IOException
	 */
	public FunctionalConfiguration toFunctionalConfiguration(Properties props) throws IOException {
		return propsMapper.readPropertiesAs(props, FunctionalConfiguration.class);
	}

	/**
	 * 
	 * @param conf
	 * @return
	 * @throws IOException
	 */
	public Properties toProperties(FunctionalConfiguration conf) throws IOException {
		return propsMapper.writeValueAsProperties(conf);
	}

	public void toYamlFile(File yamlFile, FunctionalConfiguration conf) throws IOException {
		yamlMapper.writeValue(yamlFile, conf);
	}
}
