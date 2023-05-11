package mocacong.server.config;

import lombok.RequiredArgsConstructor;
import mocacong.server.service.CafeService;
import mocacong.server.service.MemberService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final CafeService cafeService;
    private final MemberService memberService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteNotUsedImages() {
        memberService.deleteNotUsedProfileImages();
        cafeService.deleteNotUsedCafeImages();
    }
}
