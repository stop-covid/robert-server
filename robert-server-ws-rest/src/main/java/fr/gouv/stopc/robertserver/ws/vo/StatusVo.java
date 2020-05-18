package fr.gouv.stopc.robertserver.ws.vo;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class StatusVo extends AuthRequestVo {

    @Getter
    @Setter
    @NotNull
    @ToString.Exclude
    private String clientPublicECDHKey;

    @Builder
    public StatusVo(String ebid, String time, String mac, String clientPublicECDHKey) {
        super(ebid, time, mac);
        this.clientPublicECDHKey = clientPublicECDHKey;
    }
}
