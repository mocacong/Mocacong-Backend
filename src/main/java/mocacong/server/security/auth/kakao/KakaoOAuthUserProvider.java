package mocacong.server.security.auth.kakao;

import feign.FeignException;
import mocacong.server.exception.unauthorized.InvalidTokenException;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthUserProvider {

    private final KakaoAccessTokenClient kakaoAccessTokenClient;
    private final KakaoUserClient kakaoUserClient;
    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final String redirectUri;

    public KakaoOAuthUserProvider(
            KakaoAccessTokenClient kakaoAccessTokenClient,
            KakaoUserClient kakaoUserClient,
            @Value("${oauth.kakao.client-id}") String kakaoClientId,
            @Value("${oauth.kakao.client-secret}") String kakaoClientSecret,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri
    ) {
        this.kakaoAccessTokenClient = kakaoAccessTokenClient;
        this.kakaoUserClient = kakaoUserClient;
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.redirectUri = redirectUri;
    }

    public OAuthPlatformMemberResponse getKakaoPlatformMember(String authorizationCode) {
        KakaoAccessTokenRequest kakaoAccessTokenRequest =
                new KakaoAccessTokenRequest(authorizationCode, kakaoClientId, kakaoClientSecret, redirectUri);
        KakaoAccessToken token = getKakaoAccessToken(kakaoAccessTokenRequest);

        KakaoUserRequest kakaoUserRequest = new KakaoUserRequest("[\"kakao_account.email\"]");
        KakaoUser user = kakaoUserClient.getUser(kakaoUserRequest, token.getAuthorization());
        return new OAuthPlatformMemberResponse(String.valueOf(user.getId()), user.getEmail());
    }

    private KakaoAccessToken getKakaoAccessToken(KakaoAccessTokenRequest kakaoAccessTokenRequest) {
        try {
            return kakaoAccessTokenClient.getToken(kakaoAccessTokenRequest);
        } catch (FeignException e) {
            throw new InvalidTokenException("KAKAO OAuth 인가 코드가 올바르지 않습니다.");
        }
    }
}
