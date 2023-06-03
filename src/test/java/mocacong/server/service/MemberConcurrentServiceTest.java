package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.exception.badrequest.DuplicateMemberException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ServiceTest
public class MemberConcurrentServiceTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원이 동시에 여러 번 가입 시도해도 한 번만 가입된다")
    void signUpWithConcurrent() throws InterruptedException {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    memberService.signUp(request);
                } catch (DuplicateMemberException e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<Member> actual = memberRepository.findAll();
        assertAll(
                () -> assertThat(exceptions).hasSize(2),
                () -> assertThat(actual).hasSize(1)
        );
    }
}
