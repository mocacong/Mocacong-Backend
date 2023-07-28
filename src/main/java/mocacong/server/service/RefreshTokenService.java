package mocacong.server.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.RefreshToken;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
import mocacong.server.exception.unauthorized.RefreshTokenExpiredException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.RefreshTokenRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void saveTokenInfo(Long memberId, String refreshToken, String accessToken) {
        refreshTokenRepository.save(new RefreshToken(String.valueOf(memberId), refreshToken, accessToken));
    }

    @Transactional
    public void removeRefreshToken(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    public Member validateRefreshTokenAndGetMember(String refreshToken) {
        try {
            String memberId = jwtTokenProvider.getPayloadForRefreshToken(refreshToken);

            if (isRefreshTokenExpired(refreshToken)) { // 리프레시 토큰이 만료된 경우
                removeRefreshToken(refreshToken);
                throw new RefreshTokenExpiredException();
            }

            return memberRepository.findById(Long.parseLong(memberId))
                    .orElseThrow(InvalidRefreshTokenException::new);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }
    }

    private boolean isRefreshTokenExpired(String refreshToken) {
        try {
            jwtTokenProvider.getPayloadForRefreshToken(refreshToken);
            return false;
        } catch (RefreshTokenExpiredException e) {
            return true;
        }
    }
}
