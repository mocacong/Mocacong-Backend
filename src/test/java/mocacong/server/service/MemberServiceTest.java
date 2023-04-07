package mocacong.server.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.IsDuplicateEmailResponse;
import mocacong.server.dto.response.IsDuplicateNicknameResponse;
import mocacong.server.dto.response.MemberGetAllResponse;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.support.AwsS3Uploader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@ServiceTest
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AwsS3Uploader awsS3Uploader;

    @Test
    @DisplayName("회원을 정상적으로 가입한다")
    void signUp() {
        String expected = "kth990303@naver.com";
        MemberSignUpRequest request = new MemberSignUpRequest(expected, "a1b2c3d4", "케이", "010-1234-5678");

        memberService.signUp(request);

        List<Member> actual = memberRepository.findAll();
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getEmail()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 가입된 회원이 존재하면 회원 가입 시에 예외를 반환한다")
    void signUpByDuplicateMember() {
        String expected = "kth990303@naver.com";
        MemberSignUpRequest request = new MemberSignUpRequest(expected, "a1b2c3d4", "케이", "010-1234-5678");
        memberRepository.save(new Member(expected, "1234", "케이", "010-1234-5678"));

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    @DisplayName("회원 비밀번호를 정상적으로 암호화한다")
    void encryptPassword() {
        String rawPassword = "1234";

        String encryptedPassword = passwordEncoder.encode(rawPassword);

        Boolean matches = passwordEncoder.matches(rawPassword, encryptedPassword);
        assertThat(matches).isTrue();
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 8~20자가 아니면 예외를 반환한다")
    @ValueSource(strings = {"abcdef7", "abcdefgabcdefgabcde21"})
    void passwordLengthValidation(String password) {
        assertThatThrownBy(() -> memberService.signUp(new MemberSignUpRequest("kth990303@naver.com", password, "케이", "010-1234-5678")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 소문자, 숫자를 모두 포함하지 않으면 예외를 반환한다")
    @ValueSource(strings = {"abcdefgh", "12345678"})
    void passwordConfigureValidation(String password) {
        assertThatThrownBy(() -> memberService.signUp(new MemberSignUpRequest("kth990303@naver.com", password, "케이", "010-1234-5678")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("이미 존재하는 이메일인 경우 True를 반환한다")
    void isDuplicateEmailReturnTrue() {
        String email = "dlawotn3@naver.com";
        MemberSignUpRequest request = new MemberSignUpRequest(email, "a1b2c3d4", "케이", "010-1234-5678");
        memberService.signUp(request);

        IsDuplicateEmailResponse response = memberService.isDuplicateEmail(email);

        assertThat(response.isResult()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일인 경우 False를 반환한다")
    void isDuplicateEmailReturnFalse() {
        String email = "dlawotn3@naver.com";

        IsDuplicateEmailResponse response = memberService.isDuplicateEmail(email);

        assertThat(response.isResult()).isFalse();
    }

    @Test
    @DisplayName("이미 존재하는 닉네임인 경우 True를 반환한다")
    void isDuplicateNicknameReturnTrue() {
        String nickname = "메리";
        MemberSignUpRequest request = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", nickname, "010-1234-5678");
        memberService.signUp(request);

        IsDuplicateNicknameResponse response = memberService.isDuplicateNickname(nickname);

        assertThat(response.isResult()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 닉네임인 경우 False를 반환한다")
    void isDuplicateNicknameReturnFalse() {
        String nickname = "메리";

        IsDuplicateNicknameResponse response = memberService.isDuplicateNickname(nickname);

        assertThat(response.isResult()).isFalse();
    }

    @Test
    @DisplayName("닉네임의 길이가 0인 경우 예외를 던진다")
    void nicknameLengthIs0ReturnException() {
        assertThatThrownBy(() -> memberService.isDuplicateNickname(""))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    @DisplayName("회원을 정상적으로 탈퇴한다")
    void delete() {
        Member savedMember = memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "메리", "010-1234-5678"));

        memberService.delete(savedMember.getEmail());

        List<Member> actual = memberRepository.findAll();
        assertThat(actual).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 회원을 탈퇴 시에 예외를 반환한다")
    void deleteByNotFoundMember() {
        assertThatThrownBy(() -> memberService.delete("dlawotn3@naver.com"))
                .isInstanceOf(NotFoundMemberException.class);
    }

    @Test
    @DisplayName("모든 회원을 전체 삭제한다")
    void deleteAll() {
        memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678"));
        memberRepository.save(new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678"));

        memberService.deleteAll();

        List<Member> actual = memberRepository.findAll();
        assertThat(actual).hasSize(0);
    }

    @Test
    @DisplayName("회원을 전체 조회한다")
    void getAllMembers() {
        memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678"));
        memberRepository.save(new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678"));

        MemberGetAllResponse actual = memberService.getAllMembers();

        assertThat(actual.getMembers()).hasSize(2);
    }

    @Test
    @DisplayName("회원의 프로필 이미지를 변경하면 s3 서버와 연동하여 이미지를 업로드한다")
    void updateProfileImg() throws IOException {
        String expected = "test_img.jpg";
        Member member = memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "메리", "010-1234-5678"));
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg", fileInputStream);

        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        memberService.updateProfileImage(member.getEmail(), mockMultipartFile);

        Member actual = memberRepository.findByEmail(member.getEmail())
                .orElseThrow();
        assertThat(actual.getImgUrl()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원이 프로필 이미지를 삭제하거나 null로 설정하면 프로필 이미지는 null로 설정된다")
    void updateProfileImgWithNull() {
        Member member = memberRepository.save(
                new Member("kth990303@naver.com", "a1b2c3d4", "메리", "010-1234-5678", "test.me.jpg")
        );

        memberService.updateProfileImage(member.getEmail(), null);

        Member actual = memberRepository.findByEmail(member.getEmail())
                .orElseThrow();
        assertThat(actual.getImgUrl()).isNull();
    }
}
