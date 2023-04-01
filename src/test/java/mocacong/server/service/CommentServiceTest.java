package mocacong.server.service;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
}
