package mocacong.server.security.auth;

import io.jsonwebtoken.*;
import mocacong.server.exception.unauthorized.AccessTokenExpiredException;
import mocacong.server.exception.unauthorized.InvalidAccessTokenException;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
import mocacong.server.exception.unauthorized.RefreshTokenExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final String refreshSecretKey;
    private final long validityAccessTokenInMilliseconds;
    private final long validityRefreshTokenInMilliseconds;

    private final JwtParser accessTokenJwtParser;
    private final JwtParser refreshTokenJwtParser;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                            @Value("${security.jwt.token.refresh-secret-key}") String refreshSecretKey,
                            @Value("${security.jwt.token.access-key-expire-length}")
                            long validityAccessTokenInMilliseconds,
                            @Value("${security.jwt.token.refresh-key-expire-length}")
                            long validityRefreshTokenInMilliseconds) {
        this.secretKey = secretKey;
        this.refreshSecretKey = refreshSecretKey;
        this.validityAccessTokenInMilliseconds = validityAccessTokenInMilliseconds;
        this.validityRefreshTokenInMilliseconds = validityRefreshTokenInMilliseconds;
        this.accessTokenJwtParser = Jwts.parser().setSigningKey(secretKey);
        this.refreshTokenJwtParser = Jwts.parser().setSigningKey(refreshSecretKey);
    }

    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityAccessTokenInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityRefreshTokenInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey)
                .compact();
    }

    public void validateAccessToken(String token) {
        try {
            accessTokenJwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new AccessTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }

    public String getPayload(String token) {
        try {
            return accessTokenJwtParser.parseClaimsJws(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new AccessTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }

    public String getPayloadForRefreshToken(String refreshToken) {
        try {
            return refreshTokenJwtParser.parseClaimsJws(refreshToken).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }
    }
}
