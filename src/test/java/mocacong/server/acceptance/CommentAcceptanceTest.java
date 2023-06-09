package mocacong.server.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.CommentUpdateRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.CommentResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.dto.response.FindCafeResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class CommentAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("카페에 코멘트를 작성한다")
    void saveComment() {
        String expected = "공부하기 좋아요~🥰";
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CommentSaveRequest request = new CommentSaveRequest(expected);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(request)
                .when().post("/cafes/" + mapId + "/comments")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        List<CommentResponse> comments = 카페_조회(token, mapId)
                .as(FindCafeResponse.class)
                .getComments();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo(expected);
    }

    @Test
    @DisplayName("카페 코멘트 목록을 조회한다")
    void findComments() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        카페_코멘트_작성(token, mapId, new CommentSaveRequest("댓글"));

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId + "/comments?page=0&count=20")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("카페 코멘트 목록 중 내가 작성한 코멘트만을 조회한다")
    void findOnlyMyComments() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest);
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("mery@naver.com", "a1b2c3d4", "메리");
        회원_가입(signUpRequest2);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest.getPassword());
        카페_코멘트_작성(token, mapId, new CommentSaveRequest("댓글"));
        카페_코멘트_작성(token2, mapId, new CommentSaveRequest("댓글2"));

        CommentsResponse response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/cafes/" + mapId + "/comments/me?page=0&count=20")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CommentsResponse.class);

        assertThat(response.getComments()).hasSize(1);
    }

    @Test
    @DisplayName("카페에 작성한 코멘트를 수정한다")
    void updateComment() {
        String content = "공부하기 좋아요~🥰";
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CommentSaveRequest saveRequest = new CommentSaveRequest(content);
        ExtractableResponse<Response> response = 카페_코멘트_작성(token, mapId, saveRequest);
        String expected = "조용하고 좋네요.";
        Long commentId = response.as(CommentSaveResponse.class).getId();
        CommentUpdateRequest updateRequest = new CommentUpdateRequest(expected);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .body(updateRequest)
                .when().put("/cafes/" + mapId + "/comments/" + commentId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
        List<CommentResponse> comments = 카페_조회(token, mapId)
                .as(FindCafeResponse.class)
                .getComments();

        assertAll("comments",
                () -> assertEquals(1, comments.size()),
                () -> assertEquals(expected, comments.get(0).getContent())
        );
    }

    @Test
    @DisplayName("카페에 작성한 코멘트를 삭제한다")
    void deleteComment() {
        String content = "공부하기 좋아요~🥰";
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        CommentSaveRequest saveRequest = new CommentSaveRequest(content);
        ExtractableResponse<Response> response = 카페_코멘트_작성(token, mapId, saveRequest);
        Long commentId = response.as(CommentSaveResponse.class).getId();

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().delete("/cafes/" + mapId + "/comments/" + commentId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
        List<CommentResponse> comments = 카페_조회(token, mapId)
                .as(FindCafeResponse.class)
                .getComments();

        assertThat(comments.size()).isEqualTo(0);
    }
}
