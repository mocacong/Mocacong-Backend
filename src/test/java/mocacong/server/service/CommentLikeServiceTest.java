package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.CommentLike;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentLikeSaveResponse;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundCommentLikeException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentLikeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ServiceTest
class CommentLikeServiceTest {

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("특정 댓글에 좋아요를 할 수 있다.")
    void save() {
        String email = "rlawjddn103@naver.com";
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member = new Member(email, "encodePassword", "베어");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "베어카페");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member, commentContent);
        commentRepository.save(comment);

        CommentLikeSaveResponse savedCommentLike = commentLikeService.save(member.getId(), comment.getId());

        CommentLike actual = commentLikeRepository.findById(savedCommentLike.getCommentLikeId()).orElseThrow(NotFoundCommentLikeException::new);

        assertEquals(savedCommentLike.getCommentLikeId(), actual.getId());
        assertEquals(comment.getId(), actual.getComment().getId());
        assertEquals(member.getId(), actual.getMember().getId());
    }
}

