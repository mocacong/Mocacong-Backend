package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.domain.Token;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
import mocacong.server.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final long validityRefreshTokenInMilliseconds;

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Token> redisTemplate;

    public RefreshTokenService(@Value("${security.jwt.token.refresh-key-expire-length}")
                            long validityRefreshTokenInMilliseconds,
                               MemberRepository memberRepository,
                               RedisTemplate<String, Token> redisTemplate) {
        this.validityRefreshTokenInMilliseconds = validityRefreshTokenInMilliseconds;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void saveTokenInfo(Long memberId, String refreshToken, String accessToken) {
        Token token = Token.builder()
                .id(memberId)
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .expiration(validityRefreshTokenInMilliseconds) // 리프레시 토큰 유효기간
                .build();

        redisTemplate.opsForValue().set(refreshToken, token, validityRefreshTokenInMilliseconds, TimeUnit.SECONDS);
    }

    public Member getMemberFromRefreshToken(String refreshToken) {
        Token token = redisTemplate.opsForValue().get(refreshToken);
        if (token != null && token.getExpiration() > 0) {
            Long memberId = token.getId();
            return memberRepository.findById(memberId)
                    .orElseThrow(NotFoundMemberException::new);
        }
        throw new InvalidRefreshTokenException();
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Token findTokenByRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken);
    }
}
