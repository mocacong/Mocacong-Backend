package mocacong.server.infrastructure.auth;

import mocacong.server.exception.unauthorized.InvalidTokenException;
import mocacong.server.exception.unauthorized.TokenExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    private final String secretKey;
    private final long validityInMilliseconds;
    private final JwtParser jwtParser;

    public JwtUtils(@Value("${security.jwt.token.secret-key}") String secretKey,
                    @Value("${security.jwt.token.expire-length}") long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey);
    }

    public String createToken(Map<String, Object> payload) {
        Claims claims = Jwts.claims(payload);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    public String getPayload(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    public static PayloadBuilder payloadBuilder() {
        return new PayloadBuilder();
    }

    public static class PayloadBuilder {
        private final Claims claims;

        private PayloadBuilder() {
            this.claims = Jwts.claims();
        }

        public PayloadBuilder setSubject(String subject) {
            claims.setSubject(subject);
            return this;
        }

        public Map<String, Object> build() {
            return claims;
        }
    }
}
