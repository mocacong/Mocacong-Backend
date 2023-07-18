package mocacong.server.repository;

import mocacong.server.domain.Report;
import mocacong.server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByReporter(Member reporter);
}
