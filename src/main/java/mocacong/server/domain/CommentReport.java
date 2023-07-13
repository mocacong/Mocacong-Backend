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

    public CommentReport(Comment comment, Member reporter) {
        this.comment = comment;
        this.reporter = reporter;
    }

    public Member getReporter() {
        return reporter;
    }

    public void removeReporter() {
        this.reporter = null;
    }
}
