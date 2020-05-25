package fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service;

import java.util.List;

public interface IServerKeyStorageService {
    byte[] getServerKeyForEpoch(int epochId);
    byte[][] getServerKeysForEpochs(List<Integer> epochIds);
}
