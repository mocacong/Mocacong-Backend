package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.repository.MemberRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@ServiceTest
class AuthServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원 로그인 요청이 옳다면 토큰을 발급한다")
    void login() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        TokenResponse tokenResponse = authService.login(loginRequest);

        assertNotNull(tokenResponse.getToken());
    }

    @Test
    @DisplayName("회원 로그인 요청이 올바르지 않다면 예외가 발생한다")
    void loginWithException() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(email, encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);

        AuthLoginRequest loginRequest = new AuthLoginRequest(email, "wrongPassword");

        assertThrows(PasswordMismatchException.class,
                () -> authService.login(loginRequest));
    }
}
