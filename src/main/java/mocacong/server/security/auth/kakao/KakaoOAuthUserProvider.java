package mocacong.server.security.auth.kakao;

import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthUserProvider {

    private final KakaoUserClient kakaoUserClient;
    private static final String AUTHORIZATION_BEARER = "Bearer ";

    public KakaoOAuthUserProvider(KakaoUserClient kakaoUserClient) {
        this.kakaoUserClient = kakaoUserClient;
    }

    public OAuthPlatformMemberResponse getKakaoPlatformMember(String token) {
        KakaoUserRequest kakaoUserRequest = new KakaoUserRequest("[\"kakao_account.email\"]");
        KakaoUser user = kakaoUserClient.getUser(kakaoUserRequest, AUTHORIZATION_BEARER + token);

        return new OAuthPlatformMemberResponse(String.valueOf(user.getId()), user.getEmail());
    }
}
