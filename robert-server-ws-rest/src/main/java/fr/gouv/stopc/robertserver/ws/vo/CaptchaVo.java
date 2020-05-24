package fr.gouv.stopc.robertserver.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CaptchaVo {

    private String secret;

    private String response;

}