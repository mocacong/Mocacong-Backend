package mocacong.server.security.auth.apple;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.*;
import java.util.Date;
import mocacong.server.exception.unauthorized.InvalidAccessTokenException;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AppleOAuthUserProviderTest {

    @Autowired
    private AppleOAuthUserProvider appleOAuthUserProvider;

    @MockBean
    private AppleClient appleClient;
    @MockBean
    private PublicKeyGenerator publicKeyGenerator;
    @MockBean
    private AppleClaimsValidator appleClaimsValidator;

    @Test
    @DisplayName("Apple OAuth 유저 접속 시 platform Id를 반환한다")
    void getApplePlatformMember() throws NoSuchAlgorithmException {
        String expected = "19281729";
        Date now = new Date();
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .claim("email", "kth@apple.com")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setSubject(expected)
                .setExpiration(new Date(now.getTime() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        when(appleClient.getApplePublicKeys()).thenReturn(mock(ApplePublicKeys.class));
        when(publicKeyGenerator.generatePublicKey(any(), any())).thenReturn(publicKey);
        when(appleClaimsValidator.isValid(any())).thenReturn(true);

        OAuthPlatformMemberResponse actual = appleOAuthUserProvider.getApplePlatformMember(identityToken);

        assertAll(
                () -> assertThat(actual.getPlatformId()).isEqualTo(expected),
                () -> assertThat(actual.getEmail()).isEqualTo("kth@apple.com")
        );
    }

    @Test
    @DisplayName("Claim 검증에 실패할 경우 예외를 반환한다")
    void invalidClaims() throws NoSuchAlgorithmException {
        Date now = new Date();
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String identityToken = Jwts.builder()
                .setHeaderParam("kid", "W2R4HXF3K")
                .claim("id", "12345678")
                .claim("email", "kth@apple.com")
                .setIssuer("iss")
                .setIssuedAt(now)
                .setAudience("aud")
                .setSubject("19281729")
                .setExpiration(new Date(now.getTime() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        when(appleClient.getApplePublicKeys()).thenReturn(mock(ApplePublicKeys.class));
        when(publicKeyGenerator.generatePublicKey(any(), any())).thenReturn(publicKey);
        when(appleClaimsValidator.isValid(any())).thenReturn(false);

        assertThatThrownBy(() -> appleOAuthUserProvider.getApplePlatformMember(identityToken))
                .isInstanceOf(InvalidAccessTokenException.class);
    }
}
