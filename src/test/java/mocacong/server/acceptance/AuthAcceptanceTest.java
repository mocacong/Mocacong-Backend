package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.OAuthTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.kakao.KakaoOAuthUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static mocacong.server.acceptance.AcceptanceFixtures.회원_가입;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthAcceptanceTest extends AcceptanceTest {

    @MockBean
    private AppleOAuthUserProvider appleOAuthUserProvider;
    @MockBean
    private KakaoOAuthUserProvider kakaoOAuthUserProvider;

    @Test
    @DisplayName("회원이 정상적으로 로그인한다")
    void login() {
        MemberSignUpRequest request = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리");
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();
        AuthLoginRequest loginRequest = new AuthLoginRequest("dlawotn3@naver.com", "a1b2c3d4");

        TokenResponse tokenResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponse.class);

        assertAll(
                () -> assertNotNull(tokenResponse),
                () -> assertNotNull(tokenResponse.getAccessToken()),
                () -> assertNotNull(tokenResponse.getRefreshToken())
        );
    }

    @Test
    @DisplayName("회원이 Apple OAuth 로그인을 정상적으로 진행한다")
    void loginAppleOAuth() {
        String expected = "kth@apple.com";
        OAuthPlatformMemberResponse oauthResponse = new OAuthPlatformMemberResponse("1234321", expected);
        when(appleOAuthUserProvider.getApplePlatformMember(any())).thenReturn(oauthResponse);
        AppleLoginRequest request = new AppleLoginRequest("token");

        OAuthTokenResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/login/apple")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(OAuthTokenResponse.class);

        assertThat(actual.getEmail()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원이 Kakao OAuth 로그인을 정상적으로 진행한다")
    void loginKakaoOAuth() {
        String expected = "kth@kakao.com";
        OAuthPlatformMemberResponse oauthResponse = new OAuthPlatformMemberResponse("1234321", expected);
        when(kakaoOAuthUserProvider.getKakaoPlatformMember(anyString())).thenReturn(oauthResponse);
        KakaoLoginRequest request = new KakaoLoginRequest("token");

        OAuthTokenResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/login/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(OAuthTokenResponse.class);

        assertThat(actual.getEmail()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원이 정상적으로 Access Token을 재발급받는다")
    void reissueAccessToken() {
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리");
        AuthLoginRequest loginRequest = new AuthLoginRequest("dlawotn3@naver.com", "a1b2c3d4");
        회원_가입(signUpRequest);
        TokenResponse tokenResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponse.class);

        String refreshToken = tokenResponse.getRefreshToken();
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        TokenResponse response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/login/refresh")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponse.class);

        assertAll(
                () -> assertNotNull(response.getAccessToken()),
                () -> assertNotNull(response.getRefreshToken()),
                () -> assertThat(response.getRefreshToken()).isEqualTo(refreshToken)
        );
    }
}
