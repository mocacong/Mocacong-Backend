package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.exception.badrequest.DuplicateMemberException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long signUp(MemberSignUpRequest request) {
        validateDuplicateMember(request);

        Member member = new Member(request.getEmail(), request.getPassword(), request.getNickname());
        return memberRepository.save(member)
                .getId();
    }

    private void validateDuplicateMember(MemberSignUpRequest memberSignUpRequest) {
        memberRepository.findByEmail(memberSignUpRequest.getEmail())
                .ifPresent(member -> {
                    throw new DuplicateMemberException("이미 존재하는 회원입니다.");
                });
    }

    public void delete(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundMemberException("회원이 존재하지 않습니다."));
        memberRepository.delete(findMember);
    }
}
