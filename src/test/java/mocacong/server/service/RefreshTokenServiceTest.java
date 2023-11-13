package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.exception.unauthorized.InvalidRefreshTokenException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ServiceTest
class RefreshTokenServiceTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @DisplayName("올바른 refresh token 을 가지고 회원 정보를 얻는다")
    @Test
    public void getMemberFromRefreshToken() {
        String email = "dlawotn3@naver.com";
        Member member = memberRepository.save(new Member(email, "abcd1234", "메리"));
        Long payload = 1L;

        String refreshToken = refreshTokenService.createRefreshToken();
        String accessToken = jwtTokenProvider.createAccessToken(payload);
        refreshTokenService.saveTokenInfo(member.getId(), refreshToken, accessToken);
        Member findMember = refreshTokenService.getMemberFromRefreshToken(refreshToken);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(refreshToken),
                () -> Assertions.assertTrue(refreshToken.length() > 0),
                () -> assertThat(findMember.getId()).isEqualTo(payload)
        );
    }

    @DisplayName("올바르지 않은 refresh token 을 가지고 검증하면 예외를 발생시킨다")
    @Test
    public void validateWrongRefreshToken() {
        String refreshToken = "wrong-refresh-token";

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.getMemberFromRefreshToken(refreshToken)
        );
    }
}
