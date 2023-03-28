package mocacong.server.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AcceptanceFixtures {

    public static ExtractableResponse<Response> 회원_가입(MemberSignUpRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public static String 로그인_토큰_발급(String email, String password) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new AuthLoginRequest(email, password))
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponse.class)
                .getToken();
    }
}
