package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.LoginUserEmail;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.MemberSignUpResponse;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
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
    }

//    public void delete(Long memberId) {
//        Member findMember = memberRepository.findById(memberId)
//                .orElseThrow(NotFoundMemberException::new);
//        memberRepository.delete(findMember);
//    }

    public void delete(@LoginUserEmail String email){
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        memberRepository.delete(findMember);
    }
}
