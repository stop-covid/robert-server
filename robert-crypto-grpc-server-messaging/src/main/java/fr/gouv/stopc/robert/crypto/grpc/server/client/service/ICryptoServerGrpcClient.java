package fr.gouv.stopc.robert.crypto.grpc.server.client.service;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.*;


public interface ICryptoServerGrpcClient {

	void init(String host, int port);

	byte []  decryptEBID(DecryptEBIDRequest request);

	boolean  validateMacEsr(MacEsrValidationRequest request);
	
	boolean  validateMacForType(MacValidationForTypeRequest request);

	boolean validateMacHello(MacHelloValidationRequest request);

	byte decryptCountryCode(DecryptCountryCodeRequest request);
	
	Optional<GenerateIdentityResponse> generateIdentity(GenerateIdentityRequest request);

	Optional<EncryptedEphemeralTupleBundleResponse> generateEncryptedEphemeralTuple(EncryptedEphemeralTupleBundleRequest request);

	Optional<GetIdFromStatusResponse> getIdFromStatus(GetIdFromStatusRequest request);

	Optional<GetIdFromAuthResponse> getIdFromAuth(GetIdFromAuthRequest request);

	Optional<CreateRegistrationResponse> createRegistration(CreateRegistrationRequest request);

	Optional<GetInfoFromHelloMessageResponse> getInfoFromHelloMessage(GetInfoFromHelloMessageRequest request);

	Optional<DeleteIdResponse> deleteId(DeleteIdRequest request);
}
