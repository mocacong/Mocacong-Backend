package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static mocacong.server.acceptance.AcceptanceFixtures.*;

public class CommentLikeAcceptanceTest extends AcceptanceTest{
    @Test
    @DisplayName("회원이 댓글을 좋아요 한다.")
    void saveCommentLike() {
        String mapId = "12332312";
        String comment = "코딩하고 싶어지는 카페에요.";
        카페_등록(new CafeRegisterRequest(mapId, "정우네 카페"));

        MemberSignUpRequest signUpRequest1 = new MemberSignUpRequest("rlawjddn103@naver.com", "a1b2c3d4", "베어");
        회원_가입(signUpRequest1);
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest2);

        CommentSaveRequest commentRequest = new CommentSaveRequest(comment);

        String token1 = 로그인_토큰_발급(signUpRequest1.getEmail(), signUpRequest1.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest2.getPassword());
        long commentId = 카페_코멘트_작성(token1, mapId, commentRequest).body().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token2)
                .when().post("/comments/" + commentId + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원이 댓글 좋아요를 취소한다.")
    void deleteCommentLike() {
        String mapId = "12332312";
        String comment = "코딩하고 싶어지는 카페에요.";
        카페_등록(new CafeRegisterRequest(mapId, "정우네 카페"));

        MemberSignUpRequest signUpRequest1 = new MemberSignUpRequest("rlawjddn103@naver.com", "a1b2c3d4", "베어");
        회원_가입(signUpRequest1);
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        회원_가입(signUpRequest2);

        CommentSaveRequest commentRequest = new CommentSaveRequest(comment);

        String token1 = 로그인_토큰_발급(signUpRequest1.getEmail(), signUpRequest1.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest2.getPassword());
        long commentId = 카페_코멘트_작성(token1, mapId, commentRequest).body().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token2)
                .when().post("/comments/" + commentId + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token2)
                .when().delete("/comments/" + commentId + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
