package fr.gouv.stopc.robertserver.ws.vo;

import java.time.LocalDate;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobertServerKpi {

	private LocalDate date;

	@Nullable
	private Long nbAlertedUsers;

	@Nullable
	private Long nbExposedButNotAtRiskUsers;

}
