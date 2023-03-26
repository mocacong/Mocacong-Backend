package mocacong.server.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("회원을 정상적으로 가입한다")
    void signUp() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "1234", "케이", "010-1234-5678");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원이 정상적으로 탈퇴한다")
    void deleteMember() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "1234",
                "케이", "010-1234-5678");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();
        AuthLoginRequest loginRequest = new AuthLoginRequest("kth990303@naver.com", "1234");
        TokenResponse tokenResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponse.class);
        String token = tokenResponse.getToken();

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .when().delete("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("회원 탈퇴를 정상적으로 처리했습니다"));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
