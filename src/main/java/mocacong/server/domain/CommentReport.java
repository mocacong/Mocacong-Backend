package mocacong.server.domain;

import javax.persistence.*;

@Entity
@Table(name = "comment_report")
public class CommentReport {

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

    public CommentReport() {
    }

    public CommentReport(Comment comment, Member reporter) {
        this.comment = comment;
        this.reporter = reporter;
    }

    public Member getReporter() {
        return reporter;
    }
}
