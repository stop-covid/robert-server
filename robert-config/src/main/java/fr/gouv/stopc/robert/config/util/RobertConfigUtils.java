package fr.gouv.stopc.robert.config.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import fr.gouv.stopc.robert.config.dto.ComparisonResult;
import lombok.NoArgsConstructor;

/**
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@NoArgsConstructor
public class RobertConfigUtils {

	private static final String PARAMETER_COMMIT_MESSAGE_PATTERN = "\n- {0}\n Old value : {1}\n New value : {2}";

	/**
	 * Function computing the commit message to use
	 * 
	 * @param results list of configuration updates
	 * @return the computed commit message
	 * @since 0.0.1-SNAPSHOT
	 */
	public static String computeCommitMessage(Set<ComparisonResult> results) {
		StringBuffer sb = new StringBuffer("[Configuration update]");
		results.forEach(res -> sb.append(MessageFormat.format(PARAMETER_COMMIT_MESSAGE_PATTERN, res.getKey(),
				res.getCurrentValue(), res.getNewValue())));
		return sb.toString();
	}

	/**
	 * 
	 * @param confToFilter
	 * @since 0.0.1-SNAPSHOT
	 */
	public static void removeEmptyProperties(Properties confToFilter) {
		// Retrieve the filtered entries containing non empty properties
		Set<Entry<Object, Object>> filtered = confToFilter.entrySet().stream()
				.filter(x -> !StringUtils.isEmpty(x.getValue())).collect(Collectors.toSet());
		// Clear the entries of the properties
		confToFilter.entrySet().clear();
		// Fill the properties with non empty properties
		filtered.forEach(x -> confToFilter.put(x.getKey(), x.getValue()));
	}

	/**
	 * Function comparing to configuration and updating the current one when
	 * differences are found
	 * 
	 * @param appName              the name of the application
	 * @param currentConfiguration the current configuration under GIT
	 * @param newConfiguration     the new configuration to take into account
	 * @return the list of differences with key name, old & new value
	 * @since 0.0.1-SNAPSHOT
	 */
	public static List<ComparisonResult> compareAndUpdateConfigurations(Properties currentConfiguration,
			Properties newConfiguration) {
		List<ComparisonResult> result = new ArrayList<>();
		for (Entry<Object, Object> newConfEntry : newConfiguration.entrySet()) {
			// When loading a yaml file into properties, empty fields are map to a key/value
			// where value is empty. So don't take them into account they don't have to be
			// updated because not present in the yaml file
			if (currentConfiguration.containsKey(newConfEntry.getKey())
					&& !newConfEntry.getValue().equals(currentConfiguration.get(newConfEntry.getKey()))
					&& !StringUtils.isEmpty(currentConfiguration.get(newConfEntry.getKey()))) {
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
}
