package fr.gouv.stopc.robertserver.database.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HelloMessageDetail {

    @NotNull
    @ToString.Exclude
    private Long timeCollectedOnDevice;

    @NotNull
    @ToString.Exclude
    private Integer timeFromHelloMessage;

    @NotNull
    @ToString.Exclude
    private byte[] mac;

    private Integer rssiRaw;

    @NotNull
    private Integer rssiCalibrated;

}
