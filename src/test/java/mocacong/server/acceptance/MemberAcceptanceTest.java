package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static mocacong.server.acceptance.AcceptanceFixtures.로그인_토큰_발급;
import static mocacong.server.acceptance.AcceptanceFixtures.회원_가입;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MemberAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("회원을 정상적으로 가입한다")
    void signUp() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("회원이 정상적으로 탈퇴한다")
    void delete() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(request);
        String token = 로그인_토큰_발급(request.getEmail(), request.getPassword());

        RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().delete("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("모든 회원을 전체 삭제한다")
    void deleteAll() {
        MemberSignUpRequest request1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(request1);
        MemberSignUpRequest request2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
        회원_가입(request2);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/members/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원은 올바르지 않은 형식의 필드로 가입할 수 없다")
    void signUpInvalidInputField() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "abcdefgh", "케이", "010-1234-5678");
        ErrorResponse response = 회원_가입(request)
                .as(ErrorResponse.class);

        assertThat(response.getCode()).isEqualTo(1007);
    }

    @Test
    @DisplayName("가입되어 있지 않은 이메일은 이메일 중복검사에서 걸리지 않는다")
    void isDuplicateWithNonExistingEmail(){
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("이미 가입된 이메일은 이메일 중복검사에서 걸린다")
    void isDuplicateWithExistingEmail(){
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        회원_가입(request);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("길이가 0인 이메일은 이메일 중복검사에서 예외를 던진다")
    void emailLengthIs0ReturnException() {
        MemberSignUpRequest request = new MemberSignUpRequest("", "a1b2c3d4", "메리", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo(1006));
    }

    @Test
    @DisplayName("존재하지 않는 닉네임은 닉네임 중복검사에서 걸리지 않는다")
    void isDuplicateWithNonExistingNickname() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("이미 존재하는 닉네임은 닉네임 중복검사에서 걸린다")
    void isDuplicateWithExistingNickname() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("길이가 0인 닉네임은 닉네임 중복검사에서 예외를 던진다")
    void nicknameLengthIs0ReturnException() {
        MemberSignUpRequest request = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo(1009));
    }

    @Test
    @DisplayName("회원을 전체 조회한다")
    void getAllMembers() {
        MemberSignUpRequest request1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(request1);
        MemberSignUpRequest request2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
        회원_가입(request2);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/members/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body();
    }
}
