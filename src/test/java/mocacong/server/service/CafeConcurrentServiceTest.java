package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.Review;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.badrequest.DuplicateCafeException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.ReviewRepository;
import mocacong.server.service.CafeService;
import mocacong.server.service.MemberService;
import mocacong.server.service.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ServiceTest
public class CafeConcurrentServiceTest {
    @Autowired
    private CafeService cafeService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("등록되지 않은 카페를 동시에 여러 번 등록하려 해도 한 번만 등록된다")
    void saveCafeWithConcurrent() throws InterruptedException {
        CafeRegisterRequest request = new CafeRegisterRequest("20", "메리네 카페");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    cafeService.save(request);
                } catch (DuplicateCafeException e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<Cafe> actual = cafeRepository.findAll();
        assertAll(
                () -> assertThat(exceptions.isEmpty()).isFalse(),
                () -> assertThat(actual).hasSize(1)
        );
    }

    @Test
    @DisplayName("회원이 한 카페에 동시에 여러 번 평점만 등록 시도해도 한 번만 등록된다")
    void saveScoreWithConcurrent() throws InterruptedException {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CafeReviewRequest request = new CafeReviewRequest(4, null, null, null,
                null, null, null, null);

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    cafeService.saveCafeReview(member.getEmail(), cafe.getMapId(), request);
                } catch (AlreadyExistsCafeReview e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<Review> reviews = reviewRepository.findAll();
        assertAll(
                () -> assertThat(exceptions.isEmpty()).isFalse(),
                () -> assertThat(reviews).hasSize(1)
        );
    }

    @Test
    @DisplayName("회원이 한 카페에 동시에 여러 번 리뷰 등록 시도해도 한 번만 등록된다")
    void saveCafeReviewWithConcurrent() throws InterruptedException {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CafeReviewRequest request = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    cafeService.saveCafeReview(member.getEmail(), cafe.getMapId(), request);
                } catch (AlreadyExistsCafeReview e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<Review> reviews = reviewRepository.findAll();
        assertAll(
                () -> assertThat(exceptions.isEmpty()).isFalse(),
                () -> assertThat(reviews).hasSize(1)
        );
    }
}
