package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.dto.request.CafeRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class CafeAcceptanceTest extends AcceptanceTest{

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
}
