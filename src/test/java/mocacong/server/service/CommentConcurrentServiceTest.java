package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ServiceTest
public class CommentConcurrentServiceTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("타 사용자가 작성한 댓글을 동시에 여러 번 신고하려 해도 한 번만 신고된다")
    void reportCommentWithConcurrent() throws InterruptedException {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<CommentReportResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    CommentReportResponse response = commentService.report(member2.getId(), mapId, saveResponse.getId(),
                            "insult");
                    responses.add(response);
                } catch (Exception e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가

                }
                latch.countDown();
            });
        }
        latch.await();

        assertAll(
                () -> assertThat(responses.size()).isEqualTo(1),
                () -> assertThat(exceptions.size()).isEqualTo(2)
        );
    }
}
