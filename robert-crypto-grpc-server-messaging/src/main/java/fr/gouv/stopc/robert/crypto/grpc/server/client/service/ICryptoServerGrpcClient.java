package fr.gouv.stopc.robert.crypto.grpc.server.client.service;

import java.util.List;
import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.request.DecryptCountryCodeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.DecryptEBIDRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.EphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.GenerateIdentityRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.MacEsrValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.MacHelloValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.MacValidationForTypeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.response.EphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.response.GenerateIdentityResponse;

public interface ICryptoServerGrpcClient {

	void init(String host, int port);
	List<EphemeralTupleResponse> generateEphemeralTuple(EphemeralTupleRequest request);
	byte []  decryptEBID(DecryptEBIDRequest request);
	
	boolean  validateMacEsr(MacEsrValidationRequest request);
	
	boolean  validateMacForType(MacValidationForTypeRequest request);

	boolean validateMacHello(MacHelloValidationRequest request);

	byte decryptCountryCode(DecryptCountryCodeRequest request);
	
	Optional<GenerateIdentityResponse> generateIdentity(GenerateIdentityRequest request);

}
