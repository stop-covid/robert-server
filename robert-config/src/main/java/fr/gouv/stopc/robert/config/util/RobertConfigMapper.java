package fr.gouv.stopc.robert.config.util;

import java.time.ZoneId;
import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;

@Component
public class RobertConfigMapper {

	/**
	 * 
	 * @param gitCommit
	 * @return
	 */
	public ConfigurationHistoryEntry toHistoryEntry(RevCommit gitCommit) {
		Date refEntryDate = gitCommit.getAuthorIdent().getWhen();
		ZoneId zone = gitCommit.getAuthorIdent().getTimeZone().toZoneId();
		return new ConfigurationHistoryEntry(gitCommit.getFullMessage(), refEntryDate.toInstant().atZone(zone).toLocalDateTime());
	}
	
}
