package fr.gouv.stopc.robert.server.common.service.impl;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;

/**
 * Default implementation of the IServerConfigurationService
 */
@Service
public class ServerConfigurationServiceImpl implements IServerConfigurationService {



	private Long timeStartNtp;

	/**
	 * Initializes the timeStartNtp field
	 */
	@PostConstruct
	private void initTimeStartNtp() {
		LocalDate ld = LocalDate.parse("20200601", DateTimeFormatter.BASIC_ISO_DATE);
		timeStartNtp = TimeUtils.convertUnixStoNtpSeconds(ld.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
	}

	@Override
	public long getServiceTimeStart() {
		return this.timeStartNtp;
	}

	@Override
	public byte getServerCountryCode() {
		return (byte) 0x21;
	}

	@Override
	public int getEpochDurationSecs() {
		return TimeUtils.EPOCH_DURATION_SECS;
	}

	@Override
	public int getEpochBundleDurationInDays() {
		return 4;
	}

}
