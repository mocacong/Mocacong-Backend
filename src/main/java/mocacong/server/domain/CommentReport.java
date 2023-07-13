package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "comment_report", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "comment_id", "member_id" })
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_report_id")
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

    public CommentReport(Comment comment, Member reporter, ReportReason reportReason) {
        this.comment = comment;
        this.reporter = reporter;
        this.reportReason = reportReason;
    }

    public Member getReporter() {
        return reporter;
    }

    public void removeReporter() {
        this.reporter = null;
    }
}
