package mocacong.server.repository;

import mocacong.server.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RepositoryTest
class CommentLikeRepositoryTest {

    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("comment id, 멤버 id로 댓글 좋아요 id를 조회한다")
    void findByCafeIdAndMemberId() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "베어카페"));
        Member savedMember = memberRepository.save(new Member("rlawjddn103@naver.com", "abcd1234", "베어"));
        Comment savedComment = commentRepository.save(new Comment(savedCafe, savedMember, "코딩하기 좋은 카페네요."));
        CommentLike savedCommentLike = commentLikeRepository.save(new CommentLike(savedMember, savedComment));

        Long actual = commentLikeRepository.findCommentLikeIdByCommentIdAndMemberId(savedMember.getId(), savedComment.getId())
                .orElse(null);

        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isEqualTo(savedCommentLike.getId())
        );
    }

    @Test
    @DisplayName("comment id가 null인 즐겨찾기들을 모두 삭제한다")
    void deleteAllByMemberIdIsNull() {
        Member savedMember = memberRepository.save(new Member("rlawjddn103@naver.com", "abcd1234", "베어"));
        CommentLike savedCommentLike = commentLikeRepository.save(new CommentLike(savedMember, null));

        commentLikeRepository.deleteAllByCommentIdIsNull();

        List<CommentLike> actual = commentLikeRepository.findAll();
        assertThat(actual).isEmpty();
    }
}
