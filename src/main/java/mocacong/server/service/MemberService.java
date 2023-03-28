package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.MemberSignUpResponse;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,20}$");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

    public void delete(String email){
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        memberRepository.delete(findMember);
    }

    public boolean isDuplicateEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        return findMember.isPresent();
    }

    public boolean isDuplicateNickname(String nickname) {
        if (nickname == null || nickname.length() == 0) {
            throw new InvalidNicknameException();
        }
        Optional<Member> findMember = memberRepository.findByNickname(nickname);
        return findMember.isPresent();
    }
}
