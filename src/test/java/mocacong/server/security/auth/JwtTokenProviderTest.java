package mocacong.server.security.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mocacong.server.exception.unauthorized.AccessTokenExpiredException;
import mocacong.server.exception.unauthorized.InvalidAccessTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtTokenProviderTest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    private String accessToken;

    @DisplayName("payload 정보를 통해 유효한 JWT 토큰을 생성한다")
    @Test
    public void createToken() {
        Long payload = 1L;

        accessToken = jwtTokenProvider.createAccessToken(payload);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(accessToken),
                () -> Assertions.assertTrue(accessToken.length() > 0)
        );
    }

    @DisplayName("올바른 토큰 정보로 payload를 조회한다")
    @Test
    void getPayload() {
        accessToken = jwtTokenProvider.createAccessToken(1L);

        String payload = jwtTokenProvider.getPayload(accessToken);

        assertThat(jwtTokenProvider.getPayload(accessToken)).isEqualTo(payload);
    }

    @DisplayName("유효하지 않은 토큰 형식의 토큰으로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByInvalidToken() {
        String invalidToken = "invalid-token";

        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(invalidToken))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @DisplayName("만료된 토큰으로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByExpiredToken() {
        long expirationMillis = 1L;
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("secret-key",
                 expirationMillis);
        Long expiredPayload = 1L;

        String expiredToken = jwtTokenProvider.createAccessToken(expiredPayload);
        try {
            Thread.sleep(expirationMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThatThrownBy(() -> jwtTokenProvider.getPayload(expiredToken))
                .isInstanceOf(AccessTokenExpiredException.class);
    }

    @DisplayName("시크릿 키가 틀린 토큰 정보로 payload를 조회할 경우 예외를 발생시킨다")
    @Test
    void getPayloadByWrongSecretKeyToken() {
        Long payload = 1L;
        String correctSecretKey = "correct-secret-key";
        String wrongSecretKey = "wrong-secret-key";

        JwtTokenProvider tokenProvider = new JwtTokenProvider(correctSecretKey,
                3600000L);
        String token = tokenProvider.createAccessToken(payload);

        assertThatExceptionOfType(InvalidAccessTokenException.class)
                .isThrownBy(() -> {
                    JwtTokenProvider wrongTokenProvider = new JwtTokenProvider(wrongSecretKey,
                            3600000L);
                    wrongTokenProvider.getPayload(token);
                });
    }

    @DisplayName("새로운 액세스 토큰을 발급한다")
    @Test
    void renewAccessToken() {
        Long memberId = 1L;
        Date now = new Date();
        long expiredValidityInMilliseconds = 0L;
        String expiredAccessToken = Jwts.builder()
                .setExpiration(new Date(now.getTime() - expiredValidityInMilliseconds))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // 새로운 액세스 토큰 및 리프레시 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);

        Assertions.assertAll(
                () -> assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(expiredAccessToken))
                        .isInstanceOf(AccessTokenExpiredException.class),
                () -> assertThat(newAccessToken).isNotEmpty()
        );
    }
}
