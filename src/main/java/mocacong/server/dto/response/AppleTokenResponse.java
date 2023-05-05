package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class AppleTokenResponse {

    private String token;
    private String email;
    private Boolean isRegistered;
    private String platformId;
}
