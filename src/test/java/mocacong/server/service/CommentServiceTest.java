package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.exception.badrequest.InvalidCommentDeleteException;
import mocacong.server.exception.badrequest.InvalidCommentUpdateException;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ServiceTest
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("특정 카페에 댓글을 작성할 수 있다")
    void save() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        String expected = "공부하기 좋아요~🥰";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        CommentSaveResponse savedComment = commentService.save(email, mapId, expected);

        Comment actual = commentRepository.findById(savedComment.getId())
                .orElseThrow(NotFoundCommentException::new);
        assertThat(actual.getContent()).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 카페에 댓글을 작성하면 예외를 반환한다")
    void saveNotExistsCafe() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);

        assertThatThrownBy(() -> commentService.save(email, mapId, "공부하기 좋아요~🥰"))
                .isInstanceOf(NotFoundCafeException.class);
    }

    @Test
    @DisplayName("특정 카페에 댓글을 여러 번 작성할 수 있다")
    void saveManyTimes() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        commentService.save(email, mapId, "공부하기 좋아요~🥰");

        assertDoesNotThrow(() -> commentService.save(email, mapId, "공부하기 좋아요~🥰"));
    }

    @Test
    @DisplayName("특정 카페에 달린 댓글 목록을 조회한다")
    void findComments() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentRepository.save(new Comment(cafe, member, "댓글1"));
        commentRepository.save(new Comment(cafe, member, "댓글2"));
        commentRepository.save(new Comment(cafe, member, "댓글3"));
        commentRepository.save(new Comment(cafe, member, "댓글4"));

        CommentsResponse actual = commentService.findAll(email, mapId, 0, 3);

        assertAll(
                () -> assertThat(actual.getCurrentPage()).isEqualTo(0),
                () -> assertThat(actual.getComments()).hasSize(3),
                () -> assertThat(actual.getComments())
                        .extracting("content")
                        .containsExactly("댓글1", "댓글2", "댓글3")
        );
    }

    @Test
    @DisplayName("특정 카페에 달린 댓글 목록 중 내가 작성한 댓글만을 조회한다")
    void findOnlyMyComments() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentRepository.save(new Comment(cafe, member, "댓글1"));
        commentRepository.save(new Comment(cafe, member2, "댓글2"));
        commentRepository.save(new Comment(cafe, member, "댓글3"));
        commentRepository.save(new Comment(cafe, member2, "댓글4"));

        CommentsResponse actual = commentService.findCafeCommentsOnlyMyComments(email, mapId, 0, 3);

        assertAll(
                () -> assertThat(actual.getCurrentPage()).isEqualTo(0),
                () -> assertThat(actual.getComments()).hasSize(2),
                () -> assertThat(actual.getComments())
                        .extracting("content")
                        .containsExactly("댓글1", "댓글3")
        );
    }

    @Test
    @DisplayName("특정 카페에 작성한 댓글을 수정한다")
    void updateComment() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        String comment = "공부하기 좋아요~🥰";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(email, mapId, comment);
        String expected = "조용하고 좋네요";

        commentService.update(email, mapId, expected, savedComment.getId());

        Comment updatedComment = commentRepository.findById(savedComment.getId())
                .orElseThrow(NotFoundCommentException::new);
        assertThat(updatedComment.getContent()).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 카페에 댓글을 여러 번 수정할 수 있다")
    void updateManyTimes() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        String comment = "공부하기 좋아요~🥰";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(email, mapId, comment);
        String expected = "조용하고 좋네요";

        commentService.update(email, mapId, expected, savedComment.getId());

        assertDoesNotThrow(() -> commentService.update(email, mapId, expected, savedComment.getId()));
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사람이 특정 댓글 수정에 접근할 경우 예외를 반환한다")
    void updateByNonWriter() {
        String email1 = "kth990303@naver.com";
        String email2 = "mery@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member(email2, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(email1, mapId, "조용하고 좋네요");

        assertThatThrownBy(() -> commentService.update(email2, mapId, "몰래 이 코멘트를 바꿔", savedComment.getId()))
                .isInstanceOf(InvalidCommentUpdateException.class);
    }

    @Test
    @DisplayName("사용자가 작성한 댓글을 삭제할 수 있다")
    void delete() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse response = commentService.save(email, mapId, "공부하기 좋아요~🥰");

        commentService.delete(email, mapId, response.getId());

        CommentsResponse actual = commentService.findAll(email, mapId, 0, 3);
        assertThat(actual.getComments()).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제를 시도할 시 예외를 반환한다")
    void deleteNotExistsComment() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        assertThatThrownBy(() -> commentService.delete(email, mapId, 9999L))
                .isInstanceOf(NotFoundCommentException.class);
    }

    @Test
    @DisplayName("타 사용자가 작성한 댓글 삭제를 시도할 시 예외를 반환한다")
    void deleteNotMyComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이", "010-1234-5678");
        Member member2 = new Member(email2, "encodePassword", "메리", "010-1234-5678");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse response = commentService.save(email1, mapId, "공부하기 좋아요~🥰");

        assertThatThrownBy(() -> commentService.delete(email2, mapId, response.getId()))
                .isInstanceOf(InvalidCommentDeleteException.class);
    }
}
