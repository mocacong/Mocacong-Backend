package mocacong.server.service;

import mocacong.server.domain.*;
import mocacong.server.exception.badrequest.AlreadyExistsCommentLike;
import mocacong.server.exception.badrequest.AlreadyExistsFavorite;
import mocacong.server.repository.*;
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
public class CommentLikeConcurrentServiceTest {

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원이 한 댓글을 동시에 여러 번 좋아요 등록 시도해도 한 번만 등록된다")
    void saveCommentLikeWithConcurrent() throws InterruptedException {
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member1 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "베어카페");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member2, commentContent);
        commentRepository.save(comment);

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    commentLikeService.save(member1.getId(), comment.getId());
                } catch (AlreadyExistsCommentLike e) {
                    exceptions.add(e); // 중복 예외를 리스트에 추가
                }
                latch.countDown();
            });
        }
        latch.await();

        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        assertAll(
                () -> assertThat(exceptions).hasSize(2),
                () -> assertThat(commentLikes).hasSize(1)
        );
    }
}
