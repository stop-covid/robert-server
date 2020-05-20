package test.fr.gouv.stopc.robertserver.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.crypto.KeyGenerator;
import javax.inject.Inject;

import org.bson.internal.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.Tuple;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.structure.impl.Crypto3DES;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoHMACSHA256;
import fr.gouv.stopc.robertserver.database.model.EpochExposition;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.impl.RegistrationService;
import fr.gouv.stopc.robertserver.ws.RobertServerWsRestApplication;
import fr.gouv.stopc.robertserver.ws.dto.EpochKeyBundleDto;
import fr.gouv.stopc.robertserver.ws.dto.EpochKeyDto;
import fr.gouv.stopc.robertserver.ws.dto.StatusResponseDto;
import fr.gouv.stopc.robertserver.ws.dto.mapper.EpochKeyBundleDtoMapper;
import fr.gouv.stopc.robertserver.ws.utils.UriConstants;
import fr.gouv.stopc.robertserver.ws.vo.StatusVo;
import lombok.extern.slf4j.Slf4j;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
		RobertServerWsRestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
@Slf4j
public class StatusControllerWsRestTest {
	@Value("${controller.path.prefix}")
	private String pathPrefix;

	@Inject
	private TestRestTemplate restTemplate;

	HttpEntity<StatusVo> requestEntity;

	private URI targetUrl;

	private StatusVo statusBody;

	private HttpHeaders headers;

	@MockBean
	private RegistrationService registrationService;

	@Autowired
	private CryptoService cryptoService;

	@MockBean
	private ICryptoServerGrpcClient cryptoServerClient;

	@MockBean
	private EpochKeyBundleDtoMapper epochKeyBundleDtoMapper;

	@Autowired
	private IServerConfigurationService serverConfigurationService;

	private int currentEpoch;

	private Tuple tuple = Tuple.newBuilder().build();

	@BeforeEach
	public void before() {

		assert (this.restTemplate != null);
		this.headers = new HttpHeaders();
		this.headers.setContentType(MediaType.APPLICATION_JSON);
		this.targetUrl = UriComponentsBuilder.fromUriString(this.pathPrefix).path(UriConstants.STATUS).build().encode().toUri();

		this.currentEpoch = this.getCurrentEpoch();
	}

	@Test
	public void testBadHttpVerb() {
		this.statusBody = StatusVo.builder().ebid(Base64.encode(new byte[4])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<String> response = this.restTemplate.exchange(this.targetUrl.toString(), HttpMethod.GET,
				this.requestEntity, String.class);

		log.info("******* Bad HTTP Verb Payload: {}", response.getBody());

		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(ArgumentMatchers.any());
	}

	@Test
	public void testBadEBIDSize() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode("ABC".getBytes())).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testAcceptsOldEBIDValueEpoch() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		// Mess up with the epoch used to create the EBID
		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch - 10);
		Registration reg = Registration.builder()
				.permanentIdentifier(idA)
				.sharedKey(kA)
				.atRisk(true)
				.isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		EpochKeyBundleDto epochKeyDto = EpochKeyBundleDto.builder()
				.epochId(120L)
				.key(EpochKeyDto.builder()
						.ebid(Base64.encode(
								"12345678".getBytes()))
						.ecc(Base64.encode(
								"1".getBytes()))
						.build())
				.build();

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch  - 10), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		doReturn(Optional.of(EphemeralTupleResponse
				.newBuilder()
				.addAllTuple(Arrays.asList(this.tuple))
				.build())).when(this.cryptoServerClient).generateEphemeralTuple(any());
		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		doReturn(Arrays.asList(epochKeyDto)). when(this.epochKeyBundleDtoMapper).convert(anyList());


		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(this.registrationService, times(1)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(1)).saveRegistration(reg);
	}

	@Test
	public void testBadEBIDValueId() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		// Use unknown Id when creating the EBID
		byte[] modifiedIdA = new byte[5];
		System.arraycopy(idA, 0, modifiedIdA, 0, 5);
		modifiedIdA[4] = 0x00;

		byte[][] reqContent = createEBIDTimeMACFor(modifiedIdA, kA, currentEpoch);

		when(this.cryptoServerClient.decryptEBID(any())).thenReturn(
				"12345678".getBytes());
		when(this.registrationService.findById(modifiedIdA)).thenReturn(Optional.empty());

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// Then
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(this.registrationService, times(1)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testBadTimeFuture() {

		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch, 0 - (this.serverConfigurationService.getRequestTimeDeltaTolerance() + 1));

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testBadTimePast() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch, 0 - (this.serverConfigurationService.getRequestTimeDeltaTolerance() + 1));

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testBadTimeSize() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode("AB".getBytes()))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testBadMACSize() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode("ABC".getBytes())).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(0)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	@Test
	public void testBadMAC() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		doReturn(Optional.of(reg)).when(this.registrationService).findById(ArgumentMatchers.any());

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		when(this.cryptoServerClient.decryptEBID(any())).thenReturn(reqContent[0]);

		// Mess up with MAC
		reqContent[2][3] = 0x00;

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// Given
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(this.registrationService, times(1)).findById(ArgumentMatchers.any());
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}

	public byte[] generateKA() {
		byte[] ka = null;

		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

			// Creating a SecureRandom object
			SecureRandom secRandom = new SecureRandom();

			// Initializing the KeyGenerator
			keyGen.init(secRandom);

			// Creating/Generating a key
			Key key = keyGen.generateKey();
			ka = key.getEncoded();

		} catch (NoSuchAlgorithmException e) {
			log.info("Problem generating KA");
		}
		return ka;
	}

	public byte[] generateKey(final int nbOfbytes) {
		byte[] rndBytes = new byte[nbOfbytes];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(rndBytes);
		return rndBytes;
	}

	private int getCurrentEpoch() {
		long tpStartInSecondsNTP = this.serverConfigurationService.getServiceTimeStart();
		return TimeUtils.getCurrentEpochFrom(tpStartInSecondsNTP);
	}

	private byte[] generateTime32(int adjustTimeInSeconds) {
		long tsInSeconds = TimeUtils.convertUnixMillistoNtpSeconds(System.currentTimeMillis());
		tsInSeconds += adjustTimeInSeconds;
		byte[] tsInSecondsB = ByteUtils.longToBytes(tsInSeconds);
		byte[] time = new byte[4];

		System.arraycopy(tsInSecondsB, 4, time, 0, 4);

		return time;
	}

	private byte[] generateHMAC(final CryptoHMACSHA256 cryptoHMACSHA256S, final byte[] argument, final DigestSaltEnum salt)
			throws Exception {

		final byte[] prefix = new byte[] { salt.getValue() };

		// HMAC-SHA256 processing
		byte[] generatedSHA256 = cryptoHMACSHA256S.encrypt(ByteUtils.addAll(prefix, argument));

		return generatedSHA256;
	}

	private byte[] generateMACforESR(byte[] ebid, byte[] time, byte[] ka) {
		// Merge arrays
		// HMAC-256
		// return hash
		byte[] agg = new byte[8 + 4];
		System.arraycopy(ebid, 0, agg, 0, ebid.length);
		System.arraycopy(time, 0, agg, ebid.length, time.length);

		byte[] mac = new byte[32];
		try {
			mac = this.generateHMAC(new CryptoHMACSHA256(ka), agg, DigestSaltEnum.STATUS);
		} catch (Exception e) {
			log.info("Problem generating SHA256");
		}
		return mac;
	}

	private byte[][] createEBIDTimeMACFor(byte[] id, byte[] ka, int currentEpoch) {
		return this.createEBIDTimeMACFor(id, ka, currentEpoch, 0);
	}

	private byte[][] createEBIDTimeMACFor(byte[] id, byte[] ka, int currentEpoch, int adjustTimeBySeconds) {
		byte[][] res = new byte[3][];
		try {
			res[0] = this.cryptoService.generateEBID(new Crypto3DES(this.serverConfigurationService.getServerKey()),
					currentEpoch, id);
			res[1] = this.generateTime32(adjustTimeBySeconds);
			res[2] = this.generateMACforESR(res[0], res[1], ka);
		} catch (Exception e) {
			log.info("Problem creating EBID, Time and MAC for test");
		}
		return res;
	}

	@Test
	public void testStatusRequestSuccessAtRisk() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA)
				.sharedKey(kA).atRisk(true).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		EpochKeyBundleDto epochKeyDto = EpochKeyBundleDto.builder()
				.epochId(120L)
				.key(EpochKeyDto.builder()
						.ebid(Base64.encode(
								"12345678".getBytes()))
						.ecc(Base64.encode(
								"1".getBytes()))
						.build())
				.build();

		doReturn(Optional.of(EphemeralTupleResponse
				.newBuilder()
				.addAllTuple(Arrays.asList(this.tuple))
				.build())).when(this.cryptoServerClient).generateEphemeralTuple(any());

		doReturn(Arrays.asList(epochKeyDto)).when(this.epochKeyBundleDtoMapper).convert(anyList());

		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// Then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().isAtRisk());
		assertNotNull(response.getBody().getIdsForEpochs());
		assertTrue(response.getBody().getIdsForEpochs().size() > 0);
		assertTrue(reg.isNotified());
		assertTrue(currentEpoch - 3 < reg.getLastStatusRequestEpoch());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(1)).saveRegistration(reg);
	}

	@Test
	public void testStatusRequestSuccessNotAtRisk() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(false).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch - 3).build();

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		EpochKeyBundleDto epochKeyDto = EpochKeyBundleDto.builder()
				.epochId(120L)
				.key(EpochKeyDto.builder()
						.ebid(Base64.encode(
								"12345678".getBytes()))
						.ecc(Base64.encode(
								"1".getBytes()))
						.build())
				.build();

		when(this.cryptoServerClient.generateEphemeralTuple(any())).thenReturn(Optional.of(EphemeralTupleResponse
				.newBuilder()
				.addAllTuple(Arrays.asList(this.tuple))
				.build()));
		when(this.epochKeyBundleDtoMapper.convert(anyList())).thenReturn(Arrays.asList(epochKeyDto));

		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// Then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(!response.getBody().isAtRisk());
		assertNotNull(response.getBody().getIdsForEpochs());
		assertTrue(response.getBody().getIdsForEpochs().size() > 0);
		assertTrue(currentEpoch - 3 < reg.getLastStatusRequestEpoch());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(1)).saveRegistration(reg);
	}

	/*
	 ** Test not relevant anymore since ESR are now allowed after a notification
	@Test
	public void testStatusRequestAlreadyNotified() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(true).isNotified(true)
				.lastStatusRequestEpoch(currentEpoch - 2).build();

		byte[] ebid = new byte[8];
		byte[] idA = new byte[5];
		System.arraycopy(reqContent[0], 3, idA, 0, 5);
		System.arraycopy(reqContent[0], 0, ebid, 0, 8);

		doReturn(Arrays.asList( EphemeralTupleResponse
				.newBuilder().build())).when(this.cryptoServerClient).generateEphemeralTuple(any());
		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		doReturn(ebid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);


		when(this.cryptoServerClient.validateMacEsr(any())).thenReturn(true);
		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);


		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// When
		// TODO: should maybe have a different HTTP error code?
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(currentEpoch - 2, reg.getLastStatusRequestEpoch());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}*/

	@Test
	public void testStatusRequestNoNewRiskSinceLastNotif() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();

		List<EpochExposition> epochExpositions = new ArrayList<>();

		// Before latest notification
		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 9)
				.expositionScores(Arrays.asList(3.00, 4.30))
				.build());

		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 4)
				.expositionScores(Arrays.asList(0.076, 0.15))
				.build());

		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 3)
				.expositionScores(Arrays.asList(0.052, 0.16))
				.build());

		Registration reg = Registration.builder()
				.permanentIdentifier(idA)
				.sharedKey(kA)
				.atRisk(true)
				.isNotified(true)
				.lastStatusRequestEpoch(currentEpoch - 3)
				.lastNotificationEpoch(currentEpoch - 8)
				.exposedEpochs(epochExpositions)
				.build();


		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		doReturn(Optional.of(reg)).when(this.registrationService).saveRegistration(reg);
		EpochKeyBundleDto epochKeyDto = EpochKeyBundleDto.builder()
				.epochId(120L)
				.key(EpochKeyDto.builder()
						.ebid(Base64.encode(
								"12345678".getBytes()))
						.ecc(Base64.encode(
								"1".getBytes()))
						.build())
				.build();

		doReturn(Optional.of(EphemeralTupleResponse
				.newBuilder()
				.addAllTuple(Arrays.asList(this.tuple))
				.build())).when(this.cryptoServerClient).generateEphemeralTuple(any());
		doReturn(Arrays.asList(epochKeyDto)).when(this.epochKeyBundleDtoMapper).convert(anyList());

		statusBody = StatusVo.builder()
				.ebid(Base64.encode(reqContent[0]))
				.time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2]))
				.build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(currentEpoch, reg.getLastStatusRequestEpoch());
		assertEquals(false, response.getBody().isAtRisk());
		assertNotNull(response.getBody().getIdsForEpochs());
		assertEquals(true, reg.isAtRisk());
		assertEquals(true, reg.isNotified());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(1)).saveRegistration(reg);
	}

	@Test
	public void testStatusRequestNewRiskSinceLastNotif() {
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();

		List<EpochExposition> epochExpositions = new ArrayList<>();

		// Before latest notification
		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 7)
				.expositionScores(Arrays.asList(3.00, 4.30))
				.build());

		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 4)
				.expositionScores(Arrays.asList(0.076, 0.15))
				.build());

		epochExpositions.add(EpochExposition.builder()
				.epochId(currentEpoch - 3)
				.expositionScores(Arrays.asList(0.052, 0.16))
				.build());

		Registration reg = Registration.builder()
				.permanentIdentifier(idA)
				.sharedKey(kA)
				.atRisk(true)
				.isNotified(true)
				.lastStatusRequestEpoch(currentEpoch - 3)
				.lastNotificationEpoch(currentEpoch - 8)
				.exposedEpochs(epochExpositions)
				.build();

		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		EpochKeyBundleDto epochKeyDto = EpochKeyBundleDto.builder()
				.epochId(120L)
				.key(EpochKeyDto.builder()
						.ebid(Base64.encode(
								"12345678".getBytes()))
						.ecc(Base64.encode(
								"1".getBytes()))
						.build())
				.build();

		doReturn(Optional.of(EphemeralTupleResponse
				.newBuilder()
				.addAllTuple(Arrays.asList(this.tuple))
				.build())).when(this.cryptoServerClient).generateEphemeralTuple(any());
		doReturn(Arrays.asList(epochKeyDto)).when(this.epochKeyBundleDtoMapper).convert(anyList());


		statusBody = StatusVo.builder()
				.ebid(Base64.encode(reqContent[0]))
				.time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2]))
				.build();

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);

		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(currentEpoch, reg.getLastStatusRequestEpoch());
		assertEquals(true, response.getBody().isAtRisk());
		assertNotNull(response.getBody().getIdsForEpochs());
		assertEquals(true, reg.isAtRisk());
		assertEquals(true, reg.isNotified());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(1)).saveRegistration(reg);
	}

	@Test
	public void testStatusRequestESRThrottle() {

		// Given
		byte[] idA = this.generateKey(5);
		byte[] kA = this.generateKA();
		Registration reg = Registration.builder().permanentIdentifier(idA).sharedKey(kA).atRisk(false).isNotified(false)
				.lastStatusRequestEpoch(currentEpoch).build();


		byte[][] reqContent = createEBIDTimeMACFor(idA, kA, currentEpoch);

		statusBody = StatusVo.builder().ebid(Base64.encode(reqContent[0])).time(Base64.encode(reqContent[1]))
				.mac(Base64.encode(reqContent[2])).build();

		byte[] decryptedEbid = new byte[8];
		System.arraycopy(idA, 0, decryptedEbid, 3, 5);
		System.arraycopy(ByteUtils.intToBytes(currentEpoch), 1, decryptedEbid, 0, 3);

		doReturn(Optional.of(reg)).when(this.registrationService).findById(idA);

		doReturn(decryptedEbid).when(this.cryptoServerClient).decryptEBID(any());

		doReturn(true).when(this.cryptoServerClient).validateMacEsr(any());

		this.requestEntity = new HttpEntity<>(this.statusBody, this.headers);


		// When
		ResponseEntity<StatusResponseDto> response = this.restTemplate.exchange(this.targetUrl.toString(),
				HttpMethod.POST, this.requestEntity, StatusResponseDto.class);

		// Then
		// TODO: should maybe have a different HTTP error code?
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(currentEpoch, reg.getLastStatusRequestEpoch());
		verify(this.registrationService, times(1)).findById(idA);
		verify(this.registrationService, times(0)).saveRegistration(reg);
	}
}
