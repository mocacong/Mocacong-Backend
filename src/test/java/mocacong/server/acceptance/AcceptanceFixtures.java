package mocacong.server.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import mocacong.server.dto.request.*;
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

    public static ExtractableResponse<Response> 카페_등록(CafeRegisterRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/cafes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 카페_조회(String token, String mapId) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 카페_리뷰_작성(String token, String mapId, CafeReviewRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().post("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 카페_리뷰_수정(String token, String mapId, CafeReviewRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().put("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 카페_코멘트_작성(String token, String mapId, CommentSaveRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().post("/cafes/" + mapId + "/comments")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 카페_코멘트_수정(String token, String mapId, Long commentId, CommentUpdateRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().put("/cafes/" + mapId + "/comments/" + commentId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 즐겨찾기_등록(String token, String mapId) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().post("/cafes/" + mapId + "/favorites/")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 회원정보_조회(String token) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/members/mypage")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }
}
