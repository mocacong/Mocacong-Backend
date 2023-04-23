package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.ExceedCommentLengthException;
import mocacong.server.exception.notfound.NotFoundMemberException;

import javax.persistence.*;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTime {

    private static final int MAXIMUM_COMMENT_LENGTH = 200;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "content", nullable = false, length = 200)
    private String content;

    public Comment(Cafe cafe, Member member, String content) {
        this.cafe = cafe;
        this.member = member;
        validateCommentLength(content);
        this.content = content;
    }

    private void validateCommentLength(String content) {
        if (content.length() > MAXIMUM_COMMENT_LENGTH) {
            throw new ExceedCommentLengthException();
        }
    }

    public String getWriterNickname() {
        return this.member.getNickname();
    }

    public String getWriterImgUrl() {
        return this.member.getImgUrl();
    }

    public boolean isWrittenByMember(Member member) {
        return this.member.equals(member);
    }

    public void updateContent(Member member, String content) {
        if (this.member.equals(member)) {
            this.content = content;
        } else {
            throw new NotFoundMemberException();
        }
    }
}
