package mocacong.server.service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;

@Getter
@RequiredArgsConstructor
public class MemberEvent {

    private final Member member;
}
