package mocacong.server.acceptance;

import io.restassured.RestAssured;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.CafeReviewResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class CafeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("카페를 정상적으로 등록한다")
    void cafeSave() {
        CafeRegisterRequest request = new CafeRegisterRequest("1", "메리네 카페");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/cafes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("특정 카페 정보를 조회한다")
    void findCafe() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("카페에 대한 리뷰를 작성한다")
    void saveCafeReview() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CafeReviewRequest request = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");

        CafeReviewResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().post("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CafeReviewResponse.class);
        assertAll(
                () -> assertThat(actual.getScore()).isEqualTo(4),
                () -> assertThat(actual.getStudyType()).isEqualTo("solo")
        );
    }

    @Test
    @DisplayName("카페에 대한 리뷰를 다시 작성할 수 없다")
    void saveCafeReviewManyTimes() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CafeReviewRequest request = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");

        카페_리뷰_작성(token, mapId, request);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().post("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
