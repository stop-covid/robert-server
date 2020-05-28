package fr.gouv.stopc.robertserver.ws.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
@Getter
public class ApplicationConfig {
//bootstrap and applicationConfig

    @Value("${controller.path.prefix}")
    private String controllerPathPrefix;

    @Value("${robert.crypto.server.host}")
    private String cryptoServerHost;

    @Value("${robert.crypto.server.port}")
    private String cryptoServerPort;

    @Value("${submission.code.server.host}")
    private String submissionCodeServerHost;

    @Value("${submission.code.server.port}")
    private String submissionCodeServerPort;

    @Value("${submission.code.server.verify.path}")
    private String submissionCodeServerVerifyPath;

    @Value("${robert.register.server.number.epoch}")
    private int registerNumberOfEpoch;

    @Value("${robert.register.server.number.hours}")
    private int registerNumberOfHours;

    @Value("${robert.register.server.number.day}")
    private int registerNumberOfDay;

    @Value("${robert.report.server.token.short.size}")
    private int reportTokenSizeMin;

    @Value("${robert.report.server.token.long.size}")
    private int reportTokenSizeMax;

    @Value("${robert.report.server.token.short.type}")
    private String reportTokenTypeShort;

    @Value("${robert.report.server.token.long.type}")
    private String reportTokenTypeLong;

    @Value("${robert.status.server.number.next.days}")
    private int statusEpochNextDays;

    @Value("${robert.status.server.number.epoch.oneday}")
    private int statusEpochDay;

    @Value("${robert.captcha.server.secret}")
    private String captchaSecret;

    @Value("${robert.captcha.server.hostname}")
    private String captchaHostname;

    @Value("${robert.captcha.server.verify.url}")
    private String captchaVerifyUrl;

    @Value("${robert.status.server.number.hours}")
    private int statusHours;

}
