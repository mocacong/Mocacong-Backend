package mocacong.server.security.auth.kakao;

import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthUserProvider {

    private final KakaoAccessTokenClient kakaoAccessTokenClient;
    private final KakaoUserClient kakaoUserClient;
    private final String kakaoClientId;
    private final String kakaoClientSecret;

    public KakaoOAuthUserProvider(
            KakaoAccessTokenClient kakaoAccessTokenClient,
            KakaoUserClient kakaoUserClient,
            @Value("${oauth.kakao.client-id}") String kakaoClientId,
            @Value("${oauth.kakao.client-secret}") String kakaoClientSecret
    ) {
        this.kakaoAccessTokenClient = kakaoAccessTokenClient;
        this.kakaoUserClient = kakaoUserClient;
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
    }

    public OAuthPlatformMemberResponse getKakaoPlatformMember(String authorizationCode, String redirectUri) {
        KakaoAccessTokenRequest kakaoAccessTokenRequest =
                new KakaoAccessTokenRequest(authorizationCode, kakaoClientId, kakaoClientSecret, redirectUri);
        KakaoAccessToken token = kakaoAccessTokenClient.getToken(kakaoAccessTokenRequest);

        KakaoUserRequest kakaoUserRequest = new KakaoUserRequest("[\"kakao_account.email\"]");
        KakaoUser user = kakaoUserClient.getUser(kakaoUserRequest, token.getAuthorization());
        return new OAuthPlatformMemberResponse(String.valueOf(user.getId()), user.getEmail());
    }
}
