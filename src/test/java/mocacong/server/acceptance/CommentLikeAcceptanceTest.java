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
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("rlawjddn103@naver.com", "a1b2c3d4", "베어");
        회원_가입(signUpRequest);
        CommentSaveRequest commentRequest = new CommentSaveRequest(comment);

        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        long commentId = 카페_코멘트_작성(token, mapId, commentRequest).body().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().post("/comments/" + commentId + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
