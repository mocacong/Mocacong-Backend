package mocacong.server.domain;

import mocacong.server.exception.badrequest.ExceedCommentLengthException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    @Test
    @DisplayName("댓글 작성자 닉네임을 반환한다")
    void getWriterNickname() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        Comment comment = new Comment(cafe, member, "안녕하세요");

        assertThat(comment.getWriterNickname()).isEqualTo(member.getNickname());
    }

    @Test
    @DisplayName("댓글 작성자 프로필 이미지 url을 반환한다")
    void getWriterImgUrl() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678", "test_img.jpg");
        Cafe cafe = new Cafe("1", "케이카페");
        Comment comment = new Comment(cafe, member, "안녕하세요");

        assertThat(comment.getWriterImgUrl()).isEqualTo(member.getImgUrl());
    }

    @Test
    @DisplayName("코멘트가 해당 회원이 작성한 게 맞는지 여부를 반환한다")
    void isWrittenByMember() {
        Member member1 = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Member member2 = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        Comment comment = new Comment(cafe, member1, "안녕하세요");

        assertAll(
                () -> assertThat(comment.isWrittenByMember(member1)).isTrue(),
                () -> assertThat(comment.isWrittenByMember(member2)).isFalse()
        );
    }
}
