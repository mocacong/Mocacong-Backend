package mocacong.server.security.auth.apple;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.HashMap;
import java.util.Map;
import mocacong.server.security.EncryptUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppleClaimsValidatorTest {

    private static final String ISS = "iss";
    private static final String CLIENT_ID = "aud";
    private static final String NONCE = "nonce";
    private static final String NONCE_KEY = "nonce";

    private final AppleClaimsValidator appleClaimsValidator = new AppleClaimsValidator(ISS, CLIENT_ID, NONCE);

    @Test
    @DisplayName("올바른 Claims 이면 true 반환한다")
    void isValid() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(NONCE_KEY, EncryptUtils.encrypt(NONCE));

        Claims claims = Jwts.claims(claimsMap)
                .setIssuer(ISS)
                .setAudience(CLIENT_ID);

        assertThat(appleClaimsValidator.isValid(claims)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("nonce, iss, aud(client_id) 중 올바르지 않은 값이 존재하면 false 반환한다")
    @CsvSource({
            "invalid, iss, aud",
            "nonce, invalid, aud",
            "nonce, iss, invalid"
    })
    void isInvalid(String nonce, String iss, String clientId) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(NONCE_KEY, EncryptUtils.encrypt(nonce));

        Claims claims = Jwts.claims(claimsMap)
                .setIssuer(iss)
                .setAudience(clientId);

        assertThat(appleClaimsValidator.isValid(claims)).isFalse();
    }
}
