package mocacong.server.service;

import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
public class CommentConcurrentServiceTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

//    @Test
//    @DisplayName("타 사용자가 작성한 댓글을 동시에 여러 번 신고하려 해도 한 번만 신고된다")
//    void reportCommentWithConcurrent() throws InterruptedException {
//        String email1 = "kth990303@naver.com";
//        String email2 = "dlawotn3@naver.com";
//        String mapId = "2143154352323";
//        Member member1 = new Member(email1, "encodePassword", "케이");
//        Member member2 = new Member(email2, "encodePassword", "메리");
//        memberRepository.save(member1);
//        memberRepository.save(member2);
//        Cafe cafe = new Cafe(mapId, "케이카페");
//        cafeRepository.save(cafe);
//        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");
//        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        CountDownLatch latch = new CountDownLatch(3);
//        List<CommentReportResponse> responses = Collections.synchronizedList(new ArrayList<>());
//
//        for (int i = 0; i < 3; i++) {
//            executorService.execute(() -> {
//                try {
//                    CommentReportResponse response = commentService.report(member2.getId(), mapId, saveResponse.getId());
//                    responses.add(response);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//        latch.await();
//
//        assertAll(
//                () -> assertThat(responses).hasSize(1),
//                () -> assertThat(responses.get(0).getReportCount()).isEqualTo(5) // 수정필요
//        );
//    }
}
