package mocacong.server.security.auth.kakao;

import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class KakaoOAuthUserProviderTest {

    private static final String AUTHORIZATION_CODE = "test";
    private static final String REDIRECT_URI = "test";

    @Autowired
    private KakaoOAuthUserProvider kakaoOAuthUserProvider;
    @MockBean
    private KakaoAccessTokenClient kakaoAccessTokenClient;
    @MockBean
    private KakaoUserClient kakaoUserClient;

    @Test
    @DisplayName("Kakao OAuth 서버와 통신하여 사용자 정보를 발급받는다")
    void getKakaoPlatformMember() {
        String email = "kth990303@naver.com";
        String platformId = "1";
        KakaoAccessToken mockAccessToken = new KakaoAccessToken("accessToken");
        when(kakaoAccessTokenClient.getToken(any())).thenReturn(mockAccessToken);
        KakaoUser mockKakaoUser = KakaoUser.of(1L, email);
        when(kakaoUserClient.getUser(any(), anyString())).thenReturn(mockKakaoUser);

        OAuthPlatformMemberResponse actual =
                kakaoOAuthUserProvider.getKakaoPlatformMember(AUTHORIZATION_CODE, REDIRECT_URI);

        assertAll(
                () -> assertThat(actual.getEmail()).isEqualTo(email),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }
}
