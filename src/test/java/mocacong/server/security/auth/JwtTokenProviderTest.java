package mocacong.server.security.auth;

import mocacong.server.exception.unauthorized.InvalidTokenException;
import mocacong.server.exception.unauthorized.TokenExpiredException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtTokenProviderTest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    private String token;

    @DisplayName("payload 정보를 통해 유효한 JWT 토큰을 생성한다")
    @Test
    public void createToken() {
        String payload = "dlawotn3@naver.com";

        String token = jwtTokenProvider.createToken(payload);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.length() > 0);
    }

    @DisplayName("올바른 토큰 정보로 payload를 조회한다")
    @Test
    void getPayload(){
        token = jwtTokenProvider.createToken("dlawotn3@naver.com");

        String payload = jwtTokenProvider.getPayload(token);

        assertThat(jwtTokenProvider.getPayload(token)).isEqualTo(payload);
    }

    @DisplayName("유효하지 않은 토큰 형식의 토큰으로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByInvalidToken(){
        String invalidToken = "invalid-token";

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @DisplayName("만료된 토큰으로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByExpiredToken(){
        long expirationMillis = 1L;
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("secret-key", expirationMillis);
        String expiredPayload = "expired-payload";

        String expiredToken = jwtTokenProvider.createToken(expiredPayload);
        try {
            Thread.sleep(expirationMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThatThrownBy(() -> jwtTokenProvider.getPayload(expiredToken))
                .isInstanceOf(TokenExpiredException.class);
    }

    @DisplayName("시크릿 키가 틀린 토큰 정보로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByWrongSecretKeyToken() {
        String payload = "dlawotn3@naver.com";
        String correctSecretKey = "correct-secret-key";
        String wrongSecretKey = "wrong-secret-key";

        JwtTokenProvider tokenProvider = new JwtTokenProvider(correctSecretKey, 3600000L);
        String token = tokenProvider.createToken(payload);

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> {
                    JwtTokenProvider wrongTokenProvider = new JwtTokenProvider(wrongSecretKey, 3600000L);
                    wrongTokenProvider.getPayload(token);
                });
    }
}
