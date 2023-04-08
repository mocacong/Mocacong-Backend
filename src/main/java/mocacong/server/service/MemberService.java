package mocacong.server.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.badrequest.InvalidEmailException;
import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.support.AwsS3Uploader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,20}$");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwsS3Uploader awsS3Uploader;

    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        validatePassword(request.getPassword());
        validateDuplicateMember(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = new Member(request.getEmail(), encodedPassword, request.getNickname(), request.getPhone());
        return new MemberSignUpResponse(memberRepository.save(member).getId());
    }

    private void validatePassword(String password) {
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new InvalidPasswordException();
        }
    }

    private void validateDuplicateMember(MemberSignUpRequest memberSignUpRequest) {
        memberRepository.findByEmail(memberSignUpRequest.getEmail())
                .ifPresent(member -> {
                    throw new DuplicateMemberException();
                });
    }

    public void delete(String email) {
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        memberRepository.delete(findMember);
    }

    @Transactional
    public void deleteAll() {
        memberRepository.deleteAll();
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

    @Transactional
    public void updateProfileImage(String email, MultipartFile profileImg) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        String profileImgUrl = profileImg == null ? null : awsS3Uploader.uploadImage(profileImg);
        member.updateProfileImgUrl(profileImgUrl);
    }
}

