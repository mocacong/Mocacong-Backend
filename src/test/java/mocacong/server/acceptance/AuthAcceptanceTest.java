package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.AppleTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.apple.ApplePlatformMemberResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthAcceptanceTest extends AcceptanceTest {

    @MockBean
    private AppleOAuthUserProvider appleOAuthUserProvider;

    @Test
    @DisplayName("회원이 정상적으로 로그인한다")
    void login() {
        MemberSignUpRequest request = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
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

        assertNotNull(tokenResponse);
        assertNotNull(tokenResponse.getToken());
    }

    @Test
    @DisplayName("회원이 Apple OAuth 로그인을 정상적으로 진행한다")
    void loginAppleOAuth() {
        String expected = "kth@apple.com";
        ApplePlatformMemberResponse oauthResponse = new ApplePlatformMemberResponse("1234321", expected);
        when(appleOAuthUserProvider.getApplePlatformMember(any())).thenReturn(oauthResponse);
        AppleLoginRequest request = new AppleLoginRequest("token");

        AppleTokenResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/login/apple")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AppleTokenResponse.class);

        assertThat(actual.getEmail()).isEqualTo(expected);
    }
}
