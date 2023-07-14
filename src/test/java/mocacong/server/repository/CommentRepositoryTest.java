package mocacong.server.repository;

import mocacong.server.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RepositoryTest
class CommentRepositoryTest {

    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("코멘트 id, 멤버 id로 댓글 좋아요 id를 조회한다")
    void findByCommentIdAndMemberId() {
        Cafe savedCafe = cafeRepository
                .save(new Cafe("1", "베어카페"));
        Member savedMember = memberRepository
                .save(new Member("rlawjddn103@naver.com", "abcd1234", "베어"));
        Comment savedComment = commentRepository
                .save(new Comment(savedCafe, savedMember, "코딩하고 싶어지네요."));
        CommentLike commentLike = new CommentLike(savedMember, savedComment);
        commentLikeRepository.save(commentLike);

        Long actual = commentLikeRepository.findCommentLikeIdByCommentIdAndMemberId(savedMember.getId(), savedComment.getId())
                .orElse(null);

        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isEqualTo(commentLike.getId())
        );
    }
}
