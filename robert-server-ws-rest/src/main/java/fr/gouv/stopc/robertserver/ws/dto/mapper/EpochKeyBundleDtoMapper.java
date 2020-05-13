package fr.gouv.stopc.robertserver.ws.dto.mapper;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import fr.gouv.stopc.robert.crypto.grpc.server.response.EphemeralTupleResponse;
import fr.gouv.stopc.robertserver.ws.dto.EpochKeyBundleDto;
import fr.gouv.stopc.robertserver.ws.dto.EpochKeyDto;

@Component
public class EpochKeyBundleDtoMapper {
	

	public EpochKeyBundleDtoMapper () {}

	public Optional<EpochKeyBundleDto> convert(EphemeralTupleResponse ephemeralTupleResponse){
		
		return Optional.ofNullable(ephemeralTupleResponse)
				.map(response -> {
					
					return EpochKeyBundleDto.builder()
						.epochId(ephemeralTupleResponse.getEpochId())
						.key(EpochKeyDto.builder()
								.ebid(Base64.getEncoder().encodeToString(
										ephemeralTupleResponse.getEbid().toByteArray()))
								.ecc(Base64.getEncoder().encodeToString(
										ephemeralTupleResponse.getEcc().toByteArray()))
								.build())
						.build();
				});
	}

	public List<EpochKeyBundleDto> convert(List<EphemeralTupleResponse> ephemeralTupleResponses){
		
		if (CollectionUtils.isEmpty(ephemeralTupleResponses)) {
			
			return Collections.emptyList();
		}

		return ephemeralTupleResponses.stream()
				.map(this::convert)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}
}
