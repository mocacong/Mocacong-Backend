package mocacong.server.domain;

import mocacong.server.exception.badrequest.ExceedCommentLengthException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    @DisplayName("코멘트 길이가 200자를 초과하면 예외를 반환한다")
    void validateCommentLength() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        assertThatThrownBy(() -> new Comment(cafe, member, createLongComment(201)))
                .isInstanceOf(ExceedCommentLengthException.class);
    }

    private String createLongComment(int length) {
        StringBuilder comment = new StringBuilder();
        while (comment.length() < length) {
            comment.append("a");
        }
        return comment.toString();
    }
}
