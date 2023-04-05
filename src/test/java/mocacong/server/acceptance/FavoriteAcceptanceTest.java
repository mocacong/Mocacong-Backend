package mocacong.server.acceptance;

import io.restassured.RestAssured;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class FavoriteAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("회원이 카페 즐겨찾기 등록을 진행한다")
    void saveFavoriteCafe() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().post("/cafes/" + mapId + "/favorites")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
