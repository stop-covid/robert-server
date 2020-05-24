package fr.gouv.stopc.robert.crypto.grpc.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClientIdentifierBundle {
    private byte[] id;
    private byte[] keyMac;
    private byte[] keyTuples;
}
