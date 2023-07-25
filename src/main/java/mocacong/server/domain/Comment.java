package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.ExceedCommentLengthException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTime {

    private static final int MAXIMUM_COMMENT_LENGTH = 200;
    private static final int REPORT_COMMENT_THRESHOLD_COUNT = 5;
    private static final String MASK_COMMENT_CONTENT = "삭제된 댓글입니다";

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

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> commentLikes = new ArrayList<>();

    @Column(name = "is_masked")
    private boolean isMasked;

    public Comment(Cafe cafe, Member member, String content) {
        this.cafe = cafe;
        this.member = member;
        validateCommentLength(content);
        this.content = content;
        this.isMasked = false;
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

    public int getReportsCount() {
        return reports.size();
    }

    public boolean isDeletedMember() {
        return member == null;
    }

    public boolean isReportThresholdExceeded() {
        return getReportsCount() >= REPORT_COMMENT_THRESHOLD_COUNT;
    }

    public boolean isDeletedCommenter() {
        return isDeletedMember() && isReportThresholdExceeded();
    }

    public boolean hasAlreadyReported(Member member) {
        return this.reports.stream()
                .anyMatch(report -> report.getReporter().equals(member));
    }

    public void maskComment() {
        this.content = MASK_COMMENT_CONTENT;
    }

    public void maskAuthor() {
        this.member = null;
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public void updateIsMasked(boolean isMasked) {
        this.isMasked= isMasked;
    }

    public int getLikeCounts() {
        return commentLikes.size();
    }

}
