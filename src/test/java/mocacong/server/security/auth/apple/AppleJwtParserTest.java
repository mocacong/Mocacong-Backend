package mocacong.server.security.auth.apple;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mocacong.server.exception.unauthorized.AccessTokenExpiredException;
import mocacong.server.exception.unauthorized.InvalidAccessTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class AppleJwtParserTest {

    private final AppleJwtParser appleJwtParser = new AppleJwtParser();

    @Test
    @DisplayName("Apple identity token으로 헤더를 파싱한다")
    void parseHeaders() throws NoSuchAlgorithmException {
        Date now = new Date();
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setExpiration(new Date(now.getTime() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        Map<String, String> actual = appleJwtParser.parseHeaders(identityToken);

        assertThat(actual).containsKeys("alg", "kid");
    }

    @Test
    @DisplayName("올바르지 않은 형식의 Apple identity token으로 헤더를 파싱하면 예외를 반환한다")
    void parseHeadersWithInvalidToken() {
        assertThatThrownBy(() -> appleJwtParser.parseHeaders("invalidToken"))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    @DisplayName("Apple identity token, PublicKey를 받아 사용자 정보가 포함된 Claims를 반환한다")
    void parsePublicKeyAndGetClaims() throws NoSuchAlgorithmException {
        String expected = "19281729";
        Date now = new Date();
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setSubject(expected)
                .setExpiration(new Date(now.getTime() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        Claims claims = appleJwtParser.parsePublicKeyAndGetClaims(identityToken, publicKey);

        assertAll(
                () -> assertThat(claims).isNotEmpty(),
                () -> assertThat(claims.getSubject()).isEqualTo(expected)
        );
    }

    @Test
    @DisplayName("만료된 Apple identity token을 받으면 Claims 획득 시에 예외를 반환한다")
    void parseExpiredTokenAndGetClaims() throws NoSuchAlgorithmException {
        String expected = "19281729";
        Date now = new Date();
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setSubject(expected)
                .setExpiration(new Date(now.getTime() - 1L))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        assertThatThrownBy(() -> appleJwtParser.parsePublicKeyAndGetClaims(identityToken, publicKey))
                .isInstanceOf(AccessTokenExpiredException.class);
    }

    @Test
    @DisplayName("올바르지 않은 public Key로 Claims 획득 시에 예외를 반환한다")
    void parseInvalidPublicKeyAndGetClaims() throws NoSuchAlgorithmException {
        Date now = new Date();
        PrivateKey privateKey = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair()
                .getPrivate();
        PublicKey differentPublicKey = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair()
                .getPublic();
        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setSubject("19281729")
                .setExpiration(new Date(now.getTime() - 1L))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        assertThatThrownBy(() -> appleJwtParser.parsePublicKeyAndGetClaims(identityToken, differentPublicKey))
                .isInstanceOf(InvalidAccessTokenException.class);
    }
}
