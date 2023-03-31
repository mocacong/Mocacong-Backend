package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.StudyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyTypeRepository extends JpaRepository<StudyType, Long> {
    @Query("select s.studyTypeValue " +
            "from StudyType s " +
            "join s.cafe c " +
            "where c.id = :cafeId and s.studyTypeValue = :studyTypeValue")
    List<String> findAllByCafeIdAndStudyTypeValue(Long cafeId, String studyTypeValue);
    StudyType findByMemberAndCafe(Member member, Cafe cafe);
}
