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
    private String robertCryptoServerHost;

    @Value("${robert.crypto.server.port}")
    private String robertCryptoServerPort;

    @Value("${submission.code.server.host}")
    private String submissionCodeServerHost;

    @Value("${submission.code.server.port}")
    private String submissionCodeServerPort;

    @Value("${submission.code.server.verify.path}")
    private String submissionCodeServerVerifyPath;

    @Value("${register.numberOfEpochs}")
    private String numberOfEpochs;

    @Value("${report.token.short.size}")
    private String tokenSizeMin;

    @Value("${report.token.long.size}")
    private String tokenSizeMax;

    @Value("${report.token.short.type}")
    private String tokenTypeShort;

    @Value("${report.token.long.type}")
    private String tokenTypeLong;

    @Value("${status.epoch.next.days}")
    private String epochNextDays;

    @Value("${status.epoch.day}")
    private String epochDay;

    @Value("$authentication.size.ebid")
    private String sizeEbid;


}
