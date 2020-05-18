package fr.gouv.stopc.robert.crypto.grpc.server.client.service;

import java.util.List;
import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DecryptCountryCodeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DecryptEBIDRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptedEphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptedEphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacEsrValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacHelloValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacValidationForTypeRequest;



public interface ICryptoServerGrpcClient {

	void init(String host, int port);
	List<EphemeralTupleResponse> generateEphemeralTuple(EphemeralTupleRequest request);
	byte []  decryptEBID(DecryptEBIDRequest request);
	
	boolean  validateMacEsr(MacEsrValidationRequest request);
	
	boolean  validateMacForType(MacValidationForTypeRequest request);

	boolean validateMacHello(MacHelloValidationRequest request);

	byte decryptCountryCode(DecryptCountryCodeRequest request);
	
	Optional<GenerateIdentityResponse> generateIdentity(GenerateIdentityRequest request);

	Optional<EncryptedEphemeralTupleResponse> generateEncryptedEphemeralTuple(EncryptedEphemeralTupleRequest request);

}
