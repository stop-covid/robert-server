package fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.impl;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.IServerKeyStorageService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class ServerKeyStorageServiceImpl implements IServerKeyStorageService {
    @Override
    public byte[] getServerKeyForEpoch(int epochId) {
        byte[] serverKey = new byte[24];
        new SecureRandom().nextBytes(serverKey);
        return serverKey;
    }

    @Override
    public byte[][] getServerKeysForEpochs(List<Integer> epochIds) {
        byte[][] matrix = new byte[epochIds.size()][24];
        for (int i = 0; i < epochIds.size(); i++) {
            matrix[i] = getServerKeyForEpoch(epochIds.get(i));
        }
        return matrix;
    }
}
