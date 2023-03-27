package mocacong.server.service;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.MemberSignUpResponse;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void delete(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        memberRepository.delete(findMember);
    }
}
