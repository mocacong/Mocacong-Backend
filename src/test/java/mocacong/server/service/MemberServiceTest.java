package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.domain.MemberProfileImage;
import mocacong.server.domain.Platform;
import mocacong.server.dto.request.MemberProfileUpdateRequest;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.request.OAuthMemberSignUpRequest;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.*;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberProfileImageRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import mocacong.server.support.AwsS3Uploader;
import mocacong.server.support.AwsSESSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ServiceTest
class MemberServiceTest {

    @Autowired
    private MemberProfileImageRepository memberProfileImageRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AwsS3Uploader awsS3Uploader;
    @MockBean
    private AwsSESSender awsSESSender;

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
    @DisplayName("이미 가입된 이메일이 존재하면 회원 가입 시에 예외를 반환한다")
    void signUpByDuplicateEmailMember() {
        String email = "kth990303@naver.com";
        memberRepository.save(new Member(email, "1234", "케이", "010-1234-5678"));
        MemberSignUpRequest request = new MemberSignUpRequest(email, "a1b2c3d4", "케이", "010-1234-5678");

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    @DisplayName("이미 가입된 닉네임이 존재하면 회원 가입 시에 예외를 반환한다")
    void signUpByDuplicateNicknameMember() {
        String nickname = "케이";
        memberRepository.save(new Member("kth2@naver.com", "1234", nickname, "010-1234-5678"));
        MemberSignUpRequest request = new MemberSignUpRequest("kth@naver.com", "a1b2c3d4", nickname, "010-1234-5678");

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateNicknameException.class);
    }

    @Test
    @DisplayName("OAuth 유저 로그인 후 정보를 입력받아 회원을 가입한다")
    void signUpByOAuthMember() {
        String email = "kth990303@naver.com";
        String platformId = "1234321";
        Member savedMember = memberRepository.save(new Member(email, Platform.APPLE, platformId));
        OAuthMemberSignUpRequest request = new OAuthMemberSignUpRequest(null, "케이", Platform.APPLE.getValue(), platformId);

        memberService.signUpByOAuthMember(request);

        Member actual = memberRepository.findByEmail(savedMember.getEmail())
                .orElseThrow();
        assertThat(actual.getNickname()).isEqualTo("케이");
    }

    @Test
    @DisplayName("OAuth 유저 로그인 후 회원가입 시 platform과 platformId 정보로 회원이 존재하지 않으면 예외를 반환한다")
    void signUpByOAuthMemberWhenInvalidPlatformInfo() {
        memberRepository.save(new Member("kth990303@naver.com", Platform.APPLE, "1234321"));
        OAuthMemberSignUpRequest request = new OAuthMemberSignUpRequest(null, "케이", Platform.APPLE.getValue(), "invalid");

        assertThatThrownBy(() -> memberService.signUpByOAuthMember(request))
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
    @DisplayName("회원의 이메일 인증을 위한 인증코드를 이메일로 전송한다")
    void sendEmailVerifyCode() {
        doNothing().when(awsSESSender).sendToVerifyEmail(anyString(), anyString());

        EmailVerifyCodeResponse actual = memberService.sendEmailVerifyCode("kth990303@naver.com");

        assertAll(
                () -> verify(awsSESSender, times(1)).sendToVerifyEmail(anyString(), anyString()),
                () -> assertThat(parseInt(actual.getCode())).isLessThanOrEqualTo(9999)
        );
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
    @DisplayName("회원을 전체 조회한다")
    void getAllMembers() {
        memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678"));
        memberRepository.save(new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678"));

        MemberGetAllResponse actual = memberService.getAllMembers();

        assertThat(actual.getMembers()).hasSize(2);
    }

    @Test
    @DisplayName("마이페이지로 내 정보를 조회한다")
    void findMyInfo() {
        String imgUrl = "test_img.jpg";
        String nickname = "케이";
        MemberProfileImage memberProfileImage = new MemberProfileImage(imgUrl);
        memberProfileImageRepository.save(memberProfileImage);
        Member member = new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678",
                memberProfileImage, Platform.MOCACONG, "1234");
        memberRepository.save(member);

        MyPageResponse actual = memberService.findMyInfo(member.getEmail());

        assertAll(
                () -> assertThat(actual.getImgUrl()).isEqualTo(imgUrl),
                () -> assertThat(actual.getNickname()).isEqualTo(nickname)
        );
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

        assertAll(
                () -> assertThat(actual.getImgUrl()).isEqualTo(expected),
                () -> assertThat(actual.getMemberProfileImage().getIsUsed()).isTrue()
        );
    }

    @Test
    @DisplayName("회원이 프로필 이미지를 삭제하거나 null로 설정하면 프로필 이미지는 null로 설정된다")
    void updateProfileImgWithNull() {
        MemberProfileImage memberProfileImage = new MemberProfileImage("test.me.jpg");
        memberProfileImageRepository.save(memberProfileImage);
        Member member = memberRepository.save(
                new Member("kth990303@naver.com", "a1b2c3d4", "메리", "010-1234-5678",
                        memberProfileImage, Platform.MOCACONG, "1234")
        );

        memberService.updateProfileImage(member.getEmail(), null);

        Member actual = memberRepository.findByEmail(member.getEmail())
                .orElseThrow();

        assertAll(
                () -> assertThat(actual.getImgUrl()).isNull(),
                () -> assertThat(actual.getMemberProfileImage()).isNull()
        );
    }

    @Test
    @DisplayName("회원이 회원 정보를 수정하면 수정된 정보로 갱신된다")
    void updateProfileInfo() {
        String email = "dlawotn3@naver.com";
        String originalPassword = "jisu0708";
        String newPassword = "jisu1234";
        String originalNickname = "mery";
        String newNickname = "케이";
        String originalPhone = "010-1111-1111";
        String newPhone = "010-1234-1234";
        Member member = new Member(email, passwordEncoder.encode(originalPassword), originalNickname, originalPhone);
        memberRepository.save(member);

        memberService.updateProfileInfo(email, new MemberProfileUpdateRequest(newNickname, newPassword, newPhone));
        Member updatedMember = memberRepository.findByEmail(member.getEmail())
                .orElseThrow();

        assertAll(
                () -> assertThat(updatedMember.getNickname()).isEqualTo(newNickname),
                () -> assertThat(passwordEncoder.matches(newPassword, updatedMember.getPassword())).isTrue(),
                () -> assertThat(updatedMember.getPhone()).isEqualTo(newPhone)
        );
    }

    @Test
    @DisplayName("회원이 잘못된 닉네임 형식으로 회원정보 수정을 시도하면 예외를 반환한다")
    void updateBadNicknameWithValidateException() {
        String email = "dlawotn3@naver.com";
        String originalPassword = "jisu0708";
        String newPassword = "jisu1234";
        String originalNickname = "mery";
        String newNickname = "케이123";
        String originalPhone = "010-1111-1111";
        String newPhone = "010-1234-1234";
        Member member = new Member(email, passwordEncoder.encode(originalPassword), originalNickname, originalPhone);
        memberRepository.save(member);

        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(newNickname, newPassword, newPhone);
        assertThatThrownBy(() -> memberService.updateProfileInfo(email, request))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    @DisplayName("회원이 잘못된 비밀번호 형식으로 회원정보 수정을 시도하면 예외를 반환한다")
    void updateBadPasswordWithValidateException() {
        String email = "dlawotn3@naver.com";
        String originalPassword = "jisu0708";
        String newPassword = "jisu";
        String originalNickname = "mery";
        String newNickname = "케이";
        String originalPhone = "010-1111-1111";
        String newPhone = "010-1234-1234";
        Member member = new Member(email, passwordEncoder.encode(originalPassword), originalNickname, originalPhone);
        memberRepository.save(member);

        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(newNickname, newPassword, newPhone);
        assertThatThrownBy(() -> memberService.updateProfileInfo(email, request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("회원이 잘못된 전화번호 형식으로 회원정보 수정을 시도하면 예외를 반환한다")
    void updateBadPhoneWithValidateException() {
        String email = "dlawotn3@naver.com";
        String originalPassword = "jisu0708";
        String newPassword = "jisu1234";
        String originalNickname = "mery";
        String newNickname = "케이";
        String originalPhone = "010-1111-1111";
        String newPhone = "010-0000";
        Member member = new Member(email, passwordEncoder.encode(originalPassword), originalNickname, originalPhone);
        memberRepository.save(member);

        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(newNickname, newPassword, newPhone);
        assertThatThrownBy(() -> memberService.updateProfileInfo(email, request))
                .isInstanceOf(InvalidPhoneException.class);
    }

    @Test
    @DisplayName("회원이 중복된 닉네임으로 회원정보 수정을 시도하면 예외를 반환한다")
    void updateDuplicateNicknameWithValidateException() {
        String email = "dlawotn3@naver.com";
        String password = "jisu0708";
        String originalNickname = "mery";
        String newNickname = "케이";
        String phone = "010-1111-1111";
        memberRepository.save(new Member(email, password, originalNickname, phone));
        memberRepository.save(new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678"));

        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(newNickname, password, phone);
        assertThatThrownBy(() -> memberService.updateProfileInfo(email, request))
                .isInstanceOf(DuplicateNicknameException.class);
    }

    @Test
    @DisplayName("사용하지 않는 회원 프로필 이미지를 삭제한다")
    void deleteMemberProfileImages() {
        List<String> notUsedImgUrls = List.of("test_img2.jpg", "test_img3.jpg");
        MemberProfileImage memberProfileImage1 = new MemberProfileImage("test_img.jpg");
        memberProfileImageRepository.save(memberProfileImage1);
        MemberProfileImage memberProfileImage2 = new MemberProfileImage(notUsedImgUrls.get(0), false);
        memberProfileImageRepository.save(memberProfileImage2);
        MemberProfileImage memberProfileImage3 = new MemberProfileImage(notUsedImgUrls.get(1), false);
        memberProfileImageRepository.save(memberProfileImage3);

        doNothing().when(awsS3Uploader).deleteImages(new DeleteNotUsedImagesEvent(notUsedImgUrls));
        memberService.deleteNotUsedProfileImages();

        List<MemberProfileImage> actual = memberProfileImageRepository.findAll();
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual).extracting("imgUrl")
                        .containsExactlyInAnyOrder("test_img.jpg")
        );
    }

    @Test
    @DisplayName("회원이 정상 비밀번호로 비밀번호 확인 인증을 성공한다")
    void verifyPasswordReturnTrue() {
        String email = "dlawotn3@naver.com";
        String password = "jisu1234";
        String nickname = "mery";
        String phone = "010-1111-1111";
        Member member = new Member(email, passwordEncoder.encode(password), nickname, phone);
        memberRepository.save(member);

        PasswordVerifyResponse actual = memberService.verifyPassword(email, password);

        assertThat(actual.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("회원이 틀린 비밀번호로 비밀번호 확인 인증을 실패한다")
    void verifyPasswordReturnFalse() {
        String email = "dlawotn3@naver.com";
        String password = "jisu1234";
        String nickname = "mery";
        String phone = "010-1111-1111";
        Member member = new Member(email, passwordEncoder.encode(password), nickname, phone);
        memberRepository.save(member);

        PasswordVerifyResponse actual = memberService.verifyPassword(email, "wrongpwd123");

        assertThat(actual.getIsSuccess()).isFalse();
    }
}
