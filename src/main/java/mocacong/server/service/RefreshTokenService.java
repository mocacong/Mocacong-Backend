package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.RefreshToken;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
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

    public Member validateRefreshTokenAndGetMember(String refreshToken) {
       String memberId = jwtTokenProvider.getPayloadForRefreshToken(refreshToken);

       return memberRepository.findById(Long.parseLong(memberId))
               .orElseThrow(InvalidRefreshTokenException::new);
    }
}
