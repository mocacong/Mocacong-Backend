package mocacong.server.security.auth;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.exception.unauthorized.AccessTokenExpiredException;
import mocacong.server.exception.unauthorized.InvalidAccessTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final long validityAccessTokenInMilliseconds;

    private final JwtParser jwtParser;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                            @Value("${security.jwt.token.access-key-expire-length}")
                            long validityAccessTokenInMilliseconds) {
        this.secretKey = secretKey;
        this.validityAccessTokenInMilliseconds = validityAccessTokenInMilliseconds;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey);
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

    public void validateAccessToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new AccessTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }

    public boolean validateIsExpiredAccessToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.info("만료 ok");
            return true;
        }
        return false;
    }

    public String getPayload(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new AccessTokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }
}
