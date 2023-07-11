package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Status;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class OAuthTokenResponse {

    private String token;
    private String email;
    private Boolean isRegistered;
    private String platformId;
    private Status status;
}
