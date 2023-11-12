package mocacong.server.service;

import mocacong.server.domain.*;
import mocacong.server.dto.response.CommentLikeSaveResponse;
import mocacong.server.exception.badrequest.AlreadyExistsCommentLike;
import mocacong.server.exception.badrequest.InvalidCommentLikeException;
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
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member1 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "베어카페", "서울시 강남구");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member2, commentContent);
        commentRepository.save(comment);

        CommentLikeSaveResponse savedCommentLike = commentLikeService.save(member1.getId(), comment.getId());

        CommentLike actual = commentLikeRepository.findById(savedCommentLike.getCommentLikeId()).orElseThrow(NotFoundCommentLikeException::new);

        assertEquals(savedCommentLike.getCommentLikeId(), actual.getId());
        assertEquals(comment.getId(), actual.getComment().getId());
        assertEquals(member1.getId(), actual.getMember().getId());
    }

    @Test
    @DisplayName("이미 좋아요한 댓글에 좋아요를 또 하면 예외를 반환한다.")
    void saveDuplicateCommentLike() {
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member1 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "베어카페", "서울시 강남구");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member2, commentContent);
        commentRepository.save(comment);

        commentLikeService.save(member1.getId(), comment.getId());

        assertThrows(AlreadyExistsCommentLike.class,() -> commentLikeService.save(member1.getId(), comment.getId()));
    }

    @Test
    @DisplayName("자신의 댓글에 좋아요를 하면 예외를 반환한다.")
    void saveInvalidCommentLike() {
        String email = "rlawjddn103@naver.com";
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member = new Member(email, "encodePassword", "베어");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "베어카페", "서울시 강남구");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member, commentContent);
        commentRepository.save(comment);

        assertThrows(InvalidCommentLikeException.class,() -> commentLikeService.save(member.getId(), comment.getId()));
    }

    @Test
    @DisplayName("회원이 댓글 좋아요를 삭제한다.")
    void delete() {
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member1 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "베어카페", "서울시 강남구");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member2, commentContent);
        commentRepository.save(comment);
        CommentLike commentLike = new CommentLike(member1, comment);
        commentLikeRepository.save(commentLike);

        commentLikeService.delete(member1.getId(), comment.getId());

        assertThat(commentLikeRepository.findById(commentLike.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원이 존재하지 않는 댓글 좋아요를 삭제할 시 오류를 반환한다.")
    void deleteNotExistCommentLike() {
        String email = "rlawjddn103@naver.com";
        String mapId = "2143154352323";
        String commentContent = "코딩하고 싶어지는 카페에요.";
        Member member = new Member(email, "encodePassword", "베어");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "베어카페", "서울시 강남구");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member, commentContent);
        commentRepository.save(comment);
        CommentLike commentLike = new CommentLike(member, comment);
        commentLikeRepository.save(commentLike);

        commentLikeService.delete(member.getId(), comment.getId());

        assertThrows(NotFoundCommentLikeException.class,() -> commentLikeService.delete(member.getId(), comment.getId()));
    }
}

