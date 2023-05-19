package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.CafeFilterResponse;
import mocacong.server.dto.response.CafeReviewResponse;
import mocacong.server.dto.response.CafeReviewUpdateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static mocacong.server.acceptance.AcceptanceFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

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
                () -> assertThat(actual.getStudyType()).isEqualTo("solo"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("카페에 대해 내가 작성한 리뷰를 조회한다")
    void findMyCafeReview() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        카페_리뷰_작성(token, mapId, new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요"));

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId + "/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
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

    @Test
    @DisplayName("등록한 카페에 대한 리뷰를 수정한다")
    void updateCafeReview() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CafeReviewRequest request1 = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        카페_리뷰_작성(token, mapId, request1);
        CafeReviewUpdateRequest request2 = new CafeReviewUpdateRequest(3, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "불편해요");

        CafeReviewUpdateResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request2)
                .when().put("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CafeReviewUpdateResponse.class);

        assertAll(
                () -> assertThat(actual.getScore()).isEqualTo(3),
                () -> assertThat(actual.getDesk()).isEqualTo("불편해요"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("등록하지 않은 카페에 대한 리뷰는 수정할 수 없다")
    void updateCafeReviewNotFoundReview() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CafeReviewUpdateRequest request = new CafeReviewUpdateRequest(3, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "불편해요");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().put("/cafes/" + mapId)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code", equalTo(3008));
    }

    @Test
    @DisplayName("카페 정보를 미리보기한다")
    void previewCafe() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1111-1111");
        회원_가입(signUpRequest1);
        회원_가입(signUpRequest2);
        String token1 = 로그인_토큰_발급(signUpRequest1.getEmail(), signUpRequest1.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest2.getPassword());
        카페_리뷰_작성(token1, mapId, new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요"));
        카페_리뷰_작성(token2, mapId, new CafeReviewRequest(2, "group", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요"));
        즐겨찾기_등록(token1, mapId);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token1)
                .when().get("/cafes/" + mapId + "/preview")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("studyTypeValue가 solo로 주어진 경우 해당 카페 목록을 필터링한다")
    void getCafesFilter() {
        String mapId1 = "12332312";
        String mapId2 = "12355412";
        String mapId3 = "18486512";
        카페_등록(new CafeRegisterRequest(mapId1, "메리네 카페 본점"));
        카페_등록(new CafeRegisterRequest(mapId2, "메리네 카페 2호점"));
        카페_등록(new CafeRegisterRequest(mapId3, "메리네 카페 3호점"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CafeReviewRequest request1 = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        CafeReviewRequest request2 = new CafeReviewRequest(2, "group", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        CafeReviewRequest request3 = new CafeReviewRequest(5, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        카페_리뷰_작성(token, mapId1, request1);
        카페_리뷰_작성(token, mapId2, request2);
        카페_리뷰_작성(token, mapId3, request3);
        CafeFilterRequest request = new CafeFilterRequest(List.of(mapId1, mapId2, mapId3));

        CafeFilterResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().get("/cafes?studytype=solo")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CafeFilterResponse.class);

        assertThat(actual.getMapIds()).containsExactlyInAnyOrder(mapId1, mapId3);
    }

    @Test
    @DisplayName("특정 카페 이미지들을 조회한다")
    void getCafeImages() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페 본점"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId + "/img?page=0&count=10")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }
}
