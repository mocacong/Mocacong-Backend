package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.*;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.*;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.DeletedMemberRepository;
import mocacong.server.repository.MemberProfileImageRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import mocacong.server.service.event.DeleteMemberEvent;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import mocacong.server.support.AwsS3Uploader;
import mocacong.server.support.AwsSESSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
    private final DeletedMemberRepository deletedMemberRepository;
    private final MemberProfileImageRepository memberProfileImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AwsS3Uploader awsS3Uploader;
    private final AwsSESSender awsSESSender;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${mocacong.nonce}")
    private String nonce;

    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        validatePassword(request.getPassword());
        validateDuplicateMember(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        try {
            Member member = new Member(request.getEmail(), encodedPassword, request.getNickname());
            return new MemberSignUpResponse(memberRepository.save(member).getId());
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMemberException();
        }
    }

    private void validateDuplicateMember(MemberSignUpRequest memberSignUpRequest) {
        if (memberRepository.existsByEmailAndPlatform(memberSignUpRequest.getEmail(), Platform.MOCACONG)) {
            throw new DuplicateMemberException();
        }
        validateDuplicateNickname(memberSignUpRequest.getNickname());
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname))
            throw new DuplicateNicknameException();
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
    public void delete(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        DeletedMember deletedMember = DeletedMember.from(findMember);
        findMember.updateProfileImgUrl(null);
        applicationEventPublisher.publishEvent(new DeleteMemberEvent(findMember));
        deletedMemberRepository.save(deletedMember);
        memberRepository.delete(findMember);
    }

    @Transactional
    public void deleteLogicallyDeletedMemberAfter30Days() {
        LocalDate thresholdLocalDate = LocalDate.now().minusDays(30);
        Instant instant = thresholdLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date thresholdDate = Date.from(instant);
        deletedMemberRepository.deleteDeletedMemberByCreatedTime(thresholdDate);
    }

    @Transactional(readOnly = true)
    public MemberGetAllResponse getAllMembers() {
        List<Member> members = memberRepository.findAll();
        List<MemberGetResponse> memberGetResponses = members.stream()
                .map(member -> new MemberGetResponse(member.getId(), member.getEmail(), member.getNickname()))
                .collect(Collectors.toList());
        return new MemberGetAllResponse(memberGetResponses);
    }

    public IsDuplicateEmailResponse isDuplicateEmail(String email) {
        validateEmail(email);

        Optional<Member> findMember = memberRepository.findByEmailAndPlatform(email, Platform.MOCACONG);
        return new IsDuplicateEmailResponse(findMember.isPresent());
    }

    private void validateEmail(String email) {
        if (email.isBlank()) {
            throw new InvalidEmailException();
        }
    }

    public EmailVerifyCodeResponse sendEmailVerifyCode(EmailVerifyCodeRequest request) {
        validateNonce(request.getNonce());
        String requestEmail = request.getEmail();
        Member member = memberRepository.findByEmailAndPlatform(requestEmail, Platform.MOCACONG)
                .orElseThrow(NotFoundMemberException::new);
        Random random = new Random();
        int randomNumber = random.nextInt(EMAIL_VERIFY_CODE_MAXIMUM_NUMBER + 1);
        String code = String.format("%04d", randomNumber);
        awsSESSender.sendToVerifyEmail(requestEmail, code);
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        return new EmailVerifyCodeResponse(accessToken, code);
    }

    @Transactional
    public void resetPassword(Long memberId, ResetPasswordRequest request) {
        validateNonce(request.getNonce());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        String updatePassword = request.getPassword();
        validatePassword(updatePassword);
        String encryptedPassword = passwordEncoder.encode(updatePassword);
        member.updatePassword(encryptedPassword);
    }

    private void validateNonce(String requestNonce) {
        if (!nonce.equals(requestNonce)) {
            throw new InvalidNonceException(requestNonce);
        }
    }

    private void validatePassword(String password) {
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new InvalidPasswordException();
        }
    }

    public IsDuplicateNicknameResponse isDuplicateNickname(String nickname) {
        validateNickname(nickname);

        Boolean isPresent = memberRepository.existsByNickname(nickname);
        return new IsDuplicateNicknameResponse(isPresent);
    }

    private void validateNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new InvalidNicknameException();
        }
    }

    public MyPageResponse findMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        return new MyPageResponse(member.getEmail(), member.getNickname(), member.getImgUrl());
    }

    @Transactional
    public void updateProfileImage(Long memberId, MultipartFile profileImg) {
        Member member = memberRepository.findById(memberId)
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
    public void updateProfileInfo(Long memberId, MemberProfileUpdateRequest request) {
        String updateNickname = request.getNickname();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        validateDuplicateNickname(updateNickname);
        member.updateProfileInfo(updateNickname);
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

    @Transactional
    public void setActiveAfter60days() {
        LocalDate thresholdLocalDate = LocalDate.now().minusDays(60);
        Instant instant = thresholdLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date thresholdDate = Date.from(instant);
        memberRepository.bulkUpdateStatus(Status.ACTIVE, Status.INACTIVE, thresholdDate);
    }

    public PasswordVerifyResponse verifyPassword(Long memberId, PasswordVerifyRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        String storedPassword = member.getPassword();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Boolean isSuccess = storedPassword.equals(encodedPassword);

        return new PasswordVerifyResponse(isSuccess);
    }

    public GetUpdateProfileInfoResponse getUpdateProfileInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        return new GetUpdateProfileInfoResponse(member.getEmail(), member.getNickname());
    }
}
