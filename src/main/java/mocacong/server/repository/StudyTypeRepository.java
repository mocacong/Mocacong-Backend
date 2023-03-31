package mocacong.server.repository;

import java.util.List;
import mocacong.server.domain.StudyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudyTypeRepository extends JpaRepository<StudyType, Long> {
    @Query("select s.studyTypeValue " +
            "from StudyType s " +
            "join s.cafe c " +
            "where c.id = :cafeId and s.studyTypeValue = :studyTypeValue")
    List<String> findAllByCafeIdAndStudyTypeValue(Long cafeId, String studyTypeValue);
}
