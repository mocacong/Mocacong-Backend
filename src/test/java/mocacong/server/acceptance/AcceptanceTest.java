package mocacong.server.acceptance;

import io.restassured.RestAssured;
import mocacong.server.support.DatabaseCleanerCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(DatabaseCleanerCallback.class)
public class AcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUP() {
        RestAssured.port = port;
    }
}
