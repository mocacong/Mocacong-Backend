package mocacong.server.acceptance;

import io.restassured.RestAssured;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
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
}
