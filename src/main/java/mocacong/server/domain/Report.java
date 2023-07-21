package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "comment_id", "member_id" })
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    private static final int MAXIMUM_COMMENT_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason")
    private ReportReason reportReason;

    @Column(name = "original_content", length = MAXIMUM_COMMENT_LENGTH)
    private String originalContent;

    public Report(Comment comment, Member reporter, ReportReason reportReason) {
        this.comment = comment;
        this.reporter = reporter;
        this.reportReason = reportReason;
        this.originalContent = comment.getContent();
    }

    public Member getReporter() {
        return reporter;
    }

    public void removeReporter() {
        this.reporter = null;
    }
}
