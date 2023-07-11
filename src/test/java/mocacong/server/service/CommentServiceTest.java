package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.exception.badrequest.DuplicateReportCommentException;
import mocacong.server.exception.badrequest.InvalidCommentDeleteException;
import mocacong.server.exception.badrequest.InvalidCommentReportException;
import mocacong.server.exception.badrequest.InvalidCommentUpdateException;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        CommentSaveResponse savedComment = commentService.save(member.getId(), mapId, expected);

        Comment actual = commentRepository.findById(savedComment.getId())
                .orElseThrow(NotFoundCommentException::new);
        assertThat(actual.getContent()).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 카페에 댓글을 작성하면 예외를 반환한다")
    void saveNotExistsCafe() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);

        assertThatThrownBy(() -> commentService.save(member.getId(), mapId, "공부하기 좋아요~🥰"))
                .isInstanceOf(NotFoundCafeException.class);
    }

    @Test
    @DisplayName("특정 카페에 댓글을 여러 번 작성할 수 있다")
    void saveManyTimes() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        commentService.save(member.getId(), mapId, "공부하기 좋아요~🥰");

        assertDoesNotThrow(() -> commentService.save(member.getId(), mapId, "공부하기 좋아요~🥰"));
    }

    @Test
    @DisplayName("특정 카페에 달린 댓글 목록을 조회한다")
    void findComments() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentRepository.save(new Comment(cafe, member, "댓글1"));
        commentRepository.save(new Comment(cafe, member, "댓글2"));
        commentRepository.save(new Comment(cafe, member, "댓글3"));
        commentRepository.save(new Comment(cafe, member, "댓글4"));

        CommentsResponse actual = commentService.findAll(member.getId(), mapId, 0, 3);

        assertAll(
                () -> assertThat(actual.getIsEnd()).isFalse(),
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
        Member member = new Member(email, "encodePassword", "케이");
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentRepository.save(new Comment(cafe, member, "댓글1"));
        commentRepository.save(new Comment(cafe, member2, "댓글2"));
        commentRepository.save(new Comment(cafe, member, "댓글3"));
        commentRepository.save(new Comment(cafe, member2, "댓글4"));

        CommentsResponse actual = commentService.findCafeCommentsOnlyMyComments(member.getId(), mapId, 0, 3);

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
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
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(member.getId(), mapId, comment);
        String expected = "조용하고 좋네요";

        commentService.update(member.getId(), mapId, expected, savedComment.getId());

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
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(member.getId(), mapId, comment);
        String expected = "조용하고 좋네요";

        commentService.update(member.getId(), mapId, expected, savedComment.getId());

        assertDoesNotThrow(() -> commentService.update(member.getId(), mapId, expected, savedComment.getId()));
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사람이 특정 댓글 수정에 접근할 경우 예외를 반환한다")
    void updateByNonWriter() {
        String email1 = "kth990303@naver.com";
        String email2 = "mery@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse savedComment = commentService.save(member1.getId(), mapId, "조용하고 좋네요");

        assertThatThrownBy(() -> commentService.update(member2.getId(), mapId, "몰래 이 코멘트를 바꿔", savedComment.getId()))
                .isInstanceOf(InvalidCommentUpdateException.class);
    }

    @Test
    @DisplayName("사용자가 작성한 댓글을 삭제할 수 있다")
    void delete() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse response = commentService.save(member.getId(), mapId, "공부하기 좋아요~🥰");

        commentService.delete(member.getId(), mapId, response.getId());
        CommentsResponse actual = commentService.findAll(member.getId(), mapId, 0, 3);

        assertThat(actual.getComments()).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제를 시도할 시 예외를 반환한다")
    void deleteNotExistsComment() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);

        assertThatThrownBy(() -> commentService.delete(member.getId(), mapId, 9999L))
                .isInstanceOf(NotFoundCommentException.class);
    }

    @Test
    @DisplayName("타 사용자가 작성한 댓글 삭제를 시도할 시 예외를 반환한다")
    void deleteNotMyComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse response = commentService.save(member1.getId(), mapId, "공부하기 좋아요~🥰");

        assertThatThrownBy(() -> commentService.delete(member2.getId(), mapId, response.getId()))
                .isInstanceOf(InvalidCommentDeleteException.class);
    }

    @Test
    @DisplayName("타 사용자가 작성한 댓글을 신고한다")
    void reportComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");

        CommentReportResponse response = commentService.report(member2.getId(), mapId, saveResponse.getId());

        assertAll(
                () -> assertThat(response.getReportCount()).isEqualTo(1),
                () -> assertThat(response.getReporterNickname()).isEqualTo(member2.getNickname()),
                () -> assertThat(response.getId()).isEqualTo(saveResponse.getId())
        );
    }

    @Test
    @DisplayName("본인이 작성한 댓글에 대해 신고를 시도할 시 예외를 반환한다")
    void reportMyComment() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member.getId(), mapId, "아~ 소설보고 싶다");

        assertThatThrownBy(() -> commentService.report(member.getId(), mapId, saveResponse.getId()))
                .isInstanceOf(InvalidCommentReportException.class);
    }

    @Test
    @DisplayName("이미 신고한 댓글에 대해 신고를 시도할 시 예외를 반환한다")
    void reportDuplicateComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");

        commentService.report(member2.getId(), mapId, saveResponse.getId());

        assertThatThrownBy(() -> commentService.report(member2.getId(), mapId, saveResponse.getId()))
                .isInstanceOf(DuplicateReportCommentException.class);
    }

    @Test
    @DisplayName("10번 이상 신고된 댓글은 삭제되며 해당 작성자의 신고 횟수가 1씩 증가한다")
    void deleteAndReport10timesReportedComment() {
        String mapId = "2143154352323";
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Member member = new Member("dlawotn" + i + "@naver.com", "encodePassword", "메리" + (char) ('A' + i));
            members.add(member);
            memberRepository.save(member);
        }
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(members.get(0).getId(), mapId, "아~ 소설보고 싶다");

        for (int i = 1; i <= 9; i++) {
            commentService.report(members.get(i).getId(), mapId, saveResponse.getId());
        }
        CommentReportResponse reportResponse = commentService.report(members.get(10).getId(), mapId, saveResponse.getId());
        CommentsResponse actual = commentService.findAll(members.get(0).getId(), mapId, 0, 3);
        Optional<Member> commenter = memberRepository.findById(1L);

        assertAll(
                () -> assertThat(reportResponse.getReportCount()).isEqualTo(10),
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(actual.getComments()).hasSize(0),
                () -> assertThat(commenter.get().getReportCount()).isEqualTo(1)
        );
    }
}
