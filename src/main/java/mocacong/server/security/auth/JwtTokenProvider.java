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
    private final long validityInMilliseconds;
    private final JwtParser accessTokenJwtParser;
    private final JwtParser refreshTokenJwtParser;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                            @Value("${security.jwt.token.refresh-secret-key}") String refreshSecretKey,
                            @Value("${security.jwt.token.expire-length}") long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.refreshSecretKey = refreshSecretKey;
        this.validityInMilliseconds = validityInMilliseconds;
        this.accessTokenJwtParser = Jwts.parser().setSigningKey(secretKey);
        this.refreshTokenJwtParser = Jwts.parser().setSigningKey(refreshSecretKey);
    }

    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        Claims claims = Jwts.claims();

        return Jwts.builder()
                .setClaims(claims)
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

    public void validateRefreshToken(String token) {
        try {
            refreshTokenJwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
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
}
