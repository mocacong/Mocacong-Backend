package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthServiceTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;


    @BeforeEach
    void setUp() {
        // TODO: 스프링 빈 초기화 시 DB Truncate 하는 로직 작성하기
        memberRepository.deleteAll();
    }

    @DisplayName("회원 로그인 요청이 옳다면 토큰을 발급한다")
    @Test
    void login() {
        String email = "kth990303@naver.com";
        String password = "1234";
        String encodedPassword = passwordEncoder.encode("1234");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        TokenResponse tokenResponse = authService.login(loginRequest);

        assertNotNull(tokenResponse.getToken());
    }

    @DisplayName("회원 로그인 요청이 올바르지 않다면 예외가 발생한다")
    @Test
    void loginWithException() {
        String email = "kth990303@naver.com";
        String password = "1234";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(email, encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);

        AuthLoginRequest loginRequest = new AuthLoginRequest(email, "wrongPassword");

        assertThrows(PasswordMismatchException.class,
                () -> authService.login(loginRequest));
    }
}
