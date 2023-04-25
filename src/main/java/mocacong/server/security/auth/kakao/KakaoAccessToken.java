package mocacong.server.security.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class KakaoAccessToken {

    private static final String AUTHORIZATION_BEARER = "Bearer ";

    @JsonProperty("access_token")
    private String accessToken;

    public String getAuthorization() {
        return AUTHORIZATION_BEARER + accessToken;
    }
}
