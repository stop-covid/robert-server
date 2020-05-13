package fr.gouv.stopc.robertserver.database.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HelloMessageDetail {
    @NotNull
    private Long timeCollectedOnDevice;

    @NotNull
    private Integer timeFromHelloMessage;

    @NotNull
    @ToString.Exclude
    private byte[] mac;

    private Integer rssiRaw;

    @NotNull
    private Integer rssiCalibrated;
}
