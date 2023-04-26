package mocacong.server.repository;

import java.util.List;
import java.util.Optional;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.cafedetail.StudyType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByMapId(String mapId);

    @Query("SELECT c FROM Cafe c WHERE c.cafeDetail.studyType = :studyType")
    List<Cafe> findByStudyTypeValue(StudyType studyType);

    @Query("select c from Favorite f " +
            "join f.cafe c " +
            "join f.member m " +
            "where m.email = :email")
    Slice<Cafe> findByMyFavoriteCafes(String email, Pageable pageRequest);
}
