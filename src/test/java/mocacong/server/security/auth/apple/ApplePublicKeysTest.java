package mocacong.server.security.auth.apple;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplePublicKeysTest {

    @Test
    @DisplayName("alg, kid 값을 받아 해당 apple public key를 반환한다")
    void getMatchesKey() {
        ApplePublicKey expected = new ApplePublicKey("kty", "kid", "use", "alg", "n", "e");

        ApplePublicKeys applePublicKeys = new ApplePublicKeys(List.of(expected));

        assertThat(applePublicKeys.getMatchesKey("alg", "kid")).isEqualTo(expected);
    }

    @Test
    @DisplayName("alg, kid 값에 잘못된 값이 들어오면 예외를 반환한다")
    void getMatchesInvalidKey() {
        ApplePublicKey expected = new ApplePublicKey("kty", "kid", "use", "alg", "n", "e");

        ApplePublicKeys applePublicKeys = new ApplePublicKeys(List.of(expected));

        assertThatThrownBy(() -> applePublicKeys.getMatchesKey("invalid", "invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
