package mocacong.server.repository;

import mocacong.server.domain.Member;
import mocacong.server.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByReporter(Member reporter);
}
