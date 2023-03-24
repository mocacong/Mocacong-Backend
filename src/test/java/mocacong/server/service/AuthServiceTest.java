package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.infrastructure.auth.JwtUtils;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthServiceTest {
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;


    @BeforeEach
    void setUp() {
        // TODO: 스프링 빈 초기화 시 DB Truncate 하는 로직 작성하기
        memberRepository.deleteAll();
    }

    @DisplayName("회원 로그인 요청이 옳다면 토큰을 발급한다.")
    @Test
    void login() {
        String email = "kth990303@naver.com";
        String password = "1234";
        String encodedPassword = passwordEncoder.encode("1234");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이", "010-1234-5678");
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtils.createToken(any())).thenReturn("token");
        final TokenResponse tokenResponse = authService.login(loginRequest);

        assertEquals("token", tokenResponse.getToken());
    }
}
