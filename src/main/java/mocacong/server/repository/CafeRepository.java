package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByMapId(String mapId);

    @Query("SELECT c FROM Cafe c JOIN c.studyTypes s WHERE s.studyTypeValue = :studyTypeValue")
    List<Cafe> findByStudyTypeValue(String studyTypeValue);
}
