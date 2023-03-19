package mocacong.server.service;

import java.util.List;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    MemberServiceTest() {
    }

    @BeforeEach
    void setUp() {
        // TODO: 스프링 빈 초기화 시 DB Truncate 하는 로직 작성하기
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원을 정상적으로 가입한다")
    void signUp() {
        String expected = "kth990303@naver.com";
        MemberSignUpRequest request = new MemberSignUpRequest(expected, "1234", "케이", "010-1234-5678");

        memberService.signUp(request);

        List<Member> actual = memberRepository.findAll();
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getEmail()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 가입된 회원이 존재하면 회원 가입 시에 예외를 반환한다")
    void signUpByDuplicateMember() {
        String expected = "kth990303@naver.com";
        MemberSignUpRequest request = new MemberSignUpRequest(expected, "1234", "케이", "010-1234-5678");
        memberRepository.save(new Member(expected, "1234", "케이", "010-1234-5678"));

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    @DisplayName("회원을 정상적으로 탈퇴한다")
    void delete() {
        Member savedMember = memberRepository.save(new Member("kth990303@naver.com", "1234", "케이", "010-1234-5678"));

        memberService.delete(savedMember.getId());

        List<Member> actual = memberRepository.findAll();
        assertThat(actual).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 회원을 탈퇴 시에 예외를 반환한다")
    void deleteByNotFoundMember() {
        assertThatThrownBy(() -> memberService.delete(9999L))
                .isInstanceOf(NotFoundMemberException.class);
    }

    @Test
    @DisplayName("회원 비밀번호를 정상적으로 암호화한다")
    void encryptPassword() {
        String rawPassword = "1234";

        String encryptedPassword = passwordEncoder.encode(rawPassword);

        Boolean matches = passwordEncoder.matches(rawPassword, encryptedPassword);
        assertThat(matches).isTrue();
    }
}
