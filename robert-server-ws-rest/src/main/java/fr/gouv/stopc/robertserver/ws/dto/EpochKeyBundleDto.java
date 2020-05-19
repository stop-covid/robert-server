package fr.gouv.stopc.robertserver.ws.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EpochKeyBundleDto implements Serializable {

	private long epochId;

	@NotNull
	private EpochKeyDto key;

}
