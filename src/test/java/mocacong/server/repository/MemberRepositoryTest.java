package mocacong.server.repository;

import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원의 OAuth 플랫폼과 플랫폼 id 값으로 회원의 id를 조회한다")
    void findIdByPlatformAndPlatformId() {
        Member member = new Member(
                "kth@apple.com",
                "a1b2c3d4",
                "케이",
                null,
                Platform.APPLE,
                "1234321"
        );
        Member savedMember = memberRepository.save(member);

        Long actual = memberRepository.findIdByPlatformAndPlatformId(Platform.APPLE, "1234321")
                .orElseThrow();

        assertThat(actual).isEqualTo(savedMember.getId());
    }

    @Test
    @DisplayName("회원의 status가 변경된지 thresholdDateTime만큼 지났으면 newStatus로 일괄 변경한다")
    void bulkUpdateStatus() {
        Member member1 = new Member("dlawotn3@naver.com", "password1", "mery");
        Member member2 = new Member("dlawotn2@naver.com", "password2", "케이");
        memberRepository.save(member1);
        memberRepository.save(member2);

        LocalDate thresholdDateTime = LocalDate.now();
        Instant instant = thresholdDateTime.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date thresholdDate = Date.from(instant);
        Status oldStatus = Status.INACTIVE;
        Status newStatus = Status.ACTIVE;
        member1.incrementMemberReportCount(); // 상태를 ACTIVE -> INACTIVE
        member2.incrementMemberReportCount(); // 상태를 ACTIVE -> INACTIVE
        memberRepository.bulkUpdateStatus(newStatus, oldStatus, thresholdDate);

        Member updatedMember1 = memberRepository.findById(member1.getId()).orElse(null);
        Member updatedMember2 = memberRepository.findById(member2.getId()).orElse(null);

        assertAll(
                () ->  assertThat(updatedMember1.getStatus()).isEqualTo(newStatus),
                () -> assertThat(updatedMember2.getStatus()).isEqualTo(newStatus)
        );
    }
}
