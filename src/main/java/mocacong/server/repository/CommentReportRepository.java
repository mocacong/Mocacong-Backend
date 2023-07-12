package mocacong.server.repository;

import mocacong.server.domain.CommentReport;
import mocacong.server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    List<CommentReport> findAllByReporter(Member reporter);
}
