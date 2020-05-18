package fr.gouv.stopc.robert.crypto.grpc.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ServerECDHBundle {

    @ToString.Exclude
    private byte [] serverPublicKey;

    @ToString.Exclude
    private byte [] generatedSharedSecret;
}
