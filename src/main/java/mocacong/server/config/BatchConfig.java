package mocacong.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.domain.Member;
import mocacong.server.domain.Status;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.CafeService;
import mocacong.server.service.MemberService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final CafeService cafeService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EntityManager em;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteNotUsedImages() {
        memberService.deleteNotUsedProfileImages();
        cafeService.deleteNotUsedCafeImages();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정에 실행
    @Transactional
    public void activateInactivateMembers() {
        LocalDate thresholdDate = LocalDate.now().minusDays(60); // 현재 날짜로부터 60일 전의 날짜를 계산
        Optional<List<Member>> inactiveMembersOptional = memberRepository.findByStatus(Status.INACTIVE);

        if (inactiveMembersOptional.isPresent()) {
            List<Member> inactiveMembers = inactiveMembersOptional.get();

            for (Member member : inactiveMembers) {
                LocalDateTime modifiedTime = member.getModifiedTime().toLocalDateTime();
                LocalDate modifiedDate = modifiedTime.toLocalDate();

                if (modifiedDate.isEqual(thresholdDate)) { // 만약 60일 간 정지된 멤버가 있다면
                    member.changeStatus(Status.ACTIVE);
                }
            }
        }
    }
}
