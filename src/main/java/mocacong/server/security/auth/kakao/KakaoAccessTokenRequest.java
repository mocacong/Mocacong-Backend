package mocacong.server.security.auth.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class KakaoAccessTokenRequest {

    private static final String KAKAO_GRANT_TYPE = "authorization_code";

    private String code;
    private String client_id;
    private String client_secret;
    private String redirect_uri;
    private String grant_type;

    public KakaoAccessTokenRequest(
            String authorizationCode, String kakaoClientId, String kakaoClientSecret, String redirectUri
    ) {
        this.code = authorizationCode;
        this.client_id = kakaoClientId;
        this.client_secret = kakaoClientSecret;
        this.redirect_uri = redirectUri;
        this.grant_type = KAKAO_GRANT_TYPE;
    }
}
