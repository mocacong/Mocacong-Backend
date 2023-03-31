package mocacong.server.repository;

import java.util.List;
import mocacong.server.domain.StudyType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTypeRepository extends JpaRepository<StudyType, Long> {
    List<StudyType> findAllByCafeIdAndStudyTypeValue(Long cafeId, String studyTypeValue);
}
