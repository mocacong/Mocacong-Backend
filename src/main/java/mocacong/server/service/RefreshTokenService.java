package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.Token;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
import mocacong.server.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Token> redisTemplate;

    @Transactional
    public void saveTokenInfo(Long memberId, String refreshToken, String accessToken) {
        Token token = Token.builder()
                .id(memberId)
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .expiration(1728000) // 리프레시 토큰 유효기간
                .build();

        redisTemplate.opsForValue().set(refreshToken, token, 1728000, TimeUnit.SECONDS);
    }

    public Member validateRefreshTokenAndGetMember(String refreshToken) {
        Token token = redisTemplate.opsForValue().get(refreshToken);
        if (token != null && token.getExpiration() > 0) {
            Long memberId = token.getId();
            Member findMember = memberRepository.findById(memberId)
                    .orElseThrow(NotFoundMemberException::new);
            return findMember;
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
