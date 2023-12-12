package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CafeImageReportResponse;
import mocacong.server.dto.response.CafeImagesSaveResponse;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.support.AwsS3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ServiceTest
public class ReportConcurrentServiceTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private CafeService cafeService;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private AwsS3Uploader awsS3Uploader;

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
        Cafe cafe = new Cafe(mapId, "케이카페", "서울시 강남구");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<CommentReportResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    CommentReportResponse response = reportService.reportComment(member2.getId(), saveResponse.getId(),
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

    @Test
    @DisplayName("타 사용자가 등록한 카페 이미지를 동시에 여러 번 신고하려 해도 한 번만 신고된다")
    void reportCafeImageWithConcurrent() throws InterruptedException, IOException {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페", "서울시 강남구");
        cafeRepository.save(cafe);

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + "test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        CafeImagesSaveResponse cafeImagesSaveResponse = cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<CafeImageReportResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    CafeImageReportResponse response = reportService.reportCafeImage(member2.getId(), 1L,
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
