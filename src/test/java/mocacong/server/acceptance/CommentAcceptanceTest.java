package mocacong.server.acceptance;

import io.restassured.RestAssured;
import java.util.List;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.CommentResponse;
import mocacong.server.dto.response.FindCafeResponse;
import static org.assertj.core.api.Assertions.assertThat;
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

        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
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
}
