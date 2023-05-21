package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.MemberProfileImage;
import mocacong.server.domain.Platform;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.*;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberProfileImageRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import mocacong.server.service.event.MemberEvent;
import mocacong.server.support.AwsS3Uploader;
import mocacong.server.support.AwsSESSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,20}$");
    private static final int EMAIL_VERIFY_CODE_MAXIMUM_NUMBER = 9999;

    private final MemberRepository memberRepository;
    private final MemberProfileImageRepository memberProfileImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwsS3Uploader awsS3Uploader;
    private final AwsSESSender awsSESSender;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${mocacong.nonce}")
    private String nonce;

    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        validatePassword(request.getPassword());
        validateDuplicateMember(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = new Member(request.getEmail(), encodedPassword, request.getNickname(), request.getPhone());
        return new MemberSignUpResponse(memberRepository.save(member).getId());
    }

    private void validateDuplicateMember(MemberSignUpRequest memberSignUpRequest) {
        memberRepository.findByEmail(memberSignUpRequest.getEmail())
                .ifPresent(member -> {
                    throw new DuplicateMemberException();
                });
        memberRepository.findByNickname(memberSignUpRequest.getNickname())
                .ifPresent(member -> {
                    throw new DuplicateNicknameException();
                });
    }

    private void validateDuplicateNickname(String nickname) {
        memberRepository.findByNickname(nickname)
                .ifPresent(member -> {
                    throw new DuplicateNicknameException();
                });
    }

    @Transactional
    public OAuthMemberSignUpResponse signUpByOAuthMember(OAuthMemberSignUpRequest request) {
        Platform platform = Platform.from(request.getPlatform());
        Member member = memberRepository.findByPlatformAndPlatformId(platform, request.getPlatformId())
                .orElseThrow(NotFoundMemberException::new);

        member.registerOAuthMember(request.getEmail(), request.getNickname());
        return new OAuthMemberSignUpResponse(member.getId());
    }

    @Transactional
    public void delete(String email) {
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        applicationEventPublisher.publishEvent(new MemberEvent(findMember));
        memberRepository.delete(findMember);
    }

    @Transactional(readOnly = true)
    public MemberGetAllResponse getAllMembers() {
        List<Member> members = memberRepository.findAll();
        List<MemberGetResponse> memberGetResponses = members.stream()
                .map(member -> new MemberGetResponse(member.getId(), member.getEmail(),
                        member.getNickname(), member.getPhone()))
                .collect(Collectors.toList());
        return new MemberGetAllResponse(memberGetResponses);
    }

    public IsDuplicateEmailResponse isDuplicateEmail(String email) {
        validateEmail(email);

        Optional<Member> findMember = memberRepository.findByEmail(email);
        return new IsDuplicateEmailResponse(findMember.isPresent());
    }

    private void validateEmail(String email) {
        if (email.isBlank()) {
            throw new InvalidEmailException();
        }
    }

    public EmailVerifyCodeResponse sendEmailVerifyCode(EmailVerifyCodeRequest request) {
        validateNonce(request.getNonce());
        String emailOwner = request.getEmail();
        Member member = memberRepository.findByEmail(emailOwner)
                .orElseThrow(NotFoundMemberException::new);
        Random random = new Random();
        int randomNumber = random.nextInt(EMAIL_VERIFY_CODE_MAXIMUM_NUMBER + 1);
        String code = String.format("%04d", randomNumber);
        awsSESSender.sendToVerifyEmail(emailOwner, code);
        return new EmailVerifyCodeResponse(member.getId(), code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validateNonce(request.getNonce());
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(NotFoundMemberException::new);
        String updatePassword = request.getPassword();
        validatePassword(updatePassword);
        String encryptedPassword = passwordEncoder.encode(updatePassword);
        member.updatePassword(encryptedPassword);
    }

    private void validateNonce(String requestNonce) {
        if (!nonce.equals(requestNonce)) {
            throw new InvalidNonceException();
        }
    }

    private void validatePassword(String password) {
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new InvalidPasswordException();
        }
    }

    public IsDuplicateNicknameResponse isDuplicateNickname(String nickname) {
        validateNickname(nickname);

        Optional<Member> findMember = memberRepository.findByNickname(nickname);
        return new IsDuplicateNicknameResponse(findMember.isPresent());
    }

    private void validateNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new InvalidNicknameException();
        }
    }

    public MyPageResponse findMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        return new MyPageResponse(member.getNickname(), member.getImgUrl());
    }

    @Transactional
    public void updateProfileImage(String email, MultipartFile profileImg) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        if (profileImg == null) {
            member.updateProfileImgUrl(null);
            return;
        }
        String profileImgUrl = awsS3Uploader.uploadImage(profileImg);
        MemberProfileImage memberProfileImage = new MemberProfileImage(profileImgUrl, true);
        memberProfileImageRepository.save(memberProfileImage);
        member.updateProfileImgUrl(memberProfileImage);
    }

    @Transactional
    public void updateProfileInfo(String email, MemberProfileUpdateRequest request) {
        String updateNickname = request.getNickname();
        String updatePassword = request.getPassword();
        String updatePhone = request.getPhone();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        validateDuplicateNickname(updateNickname);
        validatePassword(updatePassword);
        String encryptedPassword = passwordEncoder.encode(updatePassword);
        member.updateProfileInfo(updateNickname, encryptedPassword, updatePhone);
    }

    @Transactional
    public void deleteNotUsedProfileImages() {
        List<MemberProfileImage> memberProfileImages = memberProfileImageRepository.findAllByIsUsedFalse();
        List<String> imgUrls = memberProfileImages.stream()
                .map(MemberProfileImage::getImgUrl)
                .collect(Collectors.toList());
        applicationEventPublisher.publishEvent(new DeleteNotUsedImagesEvent(imgUrls));

        List<Long> ids = memberProfileImages.stream()
                .map(MemberProfileImage::getId)
                .collect(Collectors.toList());
        memberProfileImageRepository.deleteAllByIdInBatch(ids);
    }

    public PasswordVerifyResponse verifyPassword(String email, PasswordVerifyRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        String storedPassword = member.getPassword();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Boolean isSuccess = storedPassword.equals(encodedPassword);

        return new PasswordVerifyResponse(isSuccess);
    }
}
