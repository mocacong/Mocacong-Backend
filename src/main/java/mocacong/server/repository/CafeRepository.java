package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.cafedetail.StudyType;
import mocacong.server.dto.response.MyReviewCafeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByMapId(String mapId);

    @Query("SELECT c FROM Cafe c WHERE c.cafeDetail.studyType = :studyType")
    List<Cafe> findByStudyTypeValue(StudyType studyType);

    @Query("select c.mapId from Favorite f " +
            "join f.cafe c join f.member m " +
            "where c.mapId in :mapIds and m.id = :id")
    List<String> findNearCafeMapIdsByMyFavoriteCafes(Long id, List<String> mapIds);

    @Query("select c from Favorite f " +
            "join f.cafe c " +
            "join f.member m " +
            "where m.id = :id")
    Slice<Cafe> findByMyFavoriteCafes(Long id, Pageable pageRequest);

    @Query("select new mocacong.server.dto.response.MyReviewCafeResponse(c.mapId,c.name,r.cafeDetail.studyType,s.score,c.roadAddress) from Review r " +
            "join r.cafe c " +
            "join r.member m " +
            "join c.score s " +
            "where m.id = :id and s.member.id = :id")
    Slice<MyReviewCafeResponse> findMyReviewCafesById(Long id, Pageable pageRequest);
}
