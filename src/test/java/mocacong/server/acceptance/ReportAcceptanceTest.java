package mocacong.server.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CommentReportRequest;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static mocacong.server.acceptance.AcceptanceFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("타인이 작성한 코멘트를 신고한다")
    void reportComment() {
        String reportReason = "insult";
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페", "서울시 강남구", "010-1234-5678"));

        MemberSignUpRequest signUpRequest1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이");
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리");
        회원_가입(signUpRequest1);
        회원_가입(signUpRequest2);
        String token1 = 로그인_토큰_발급(signUpRequest1.getEmail(), signUpRequest1.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest2.getPassword());
        CommentSaveRequest saveRequest = new CommentSaveRequest("여길 왜 가냐");
        ExtractableResponse<Response> saveResponse = 카페_코멘트_작성(token1, mapId, saveRequest);
        Long commentId = saveResponse.as(CommentSaveResponse.class).getId();
        CommentReportRequest reportRequest = new CommentReportRequest(reportReason);

        CommentReportResponse response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token2)
                .body(reportRequest)
                .when().post("/reports/comment/" + commentId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CommentReportResponse.class);

        assertThat(response.getCommentReportCount()).isEqualTo(1);
    }
}
