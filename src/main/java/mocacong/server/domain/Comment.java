package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.ExceedCommentLengthException;

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

    @Column(name = "content", nullable = false, length = MAXIMUM_COMMENT_LENGTH)
    private String content;

    @Column(name = "report_count", nullable = false)
    private int reportCount;

    public Comment(Cafe cafe, Member member, String content) {
        this.cafe = cafe;
        this.member = member;
        validateCommentLength(content);
        this.content = content;
        this.reportCount = 0;
    }

    private void validateCommentLength(String content) {
        if (content.length() > MAXIMUM_COMMENT_LENGTH) {
            throw new ExceedCommentLengthException();
        }
    }

    public String getWriterNickname() {
        return this.member != null ? this.member.getNickname() : null;
    }

    public String getWriterImgUrl() {
        return this.member != null ? this.member.getImgUrl() : null;
    }

    public boolean isWrittenByMember(Member member) {
        return this.member != null && this.member.equals(member);
    }

    public void updateComment(String content) {
        validateCommentLength(content);
        this.content = content;
    }

    public void removeMember() {
        this.member = null;
    }

    public void incrementReportCount() {
        reportCount++;
    }
}
