package mocacong.server.repository;

import mocacong.server.domain.CafeImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CafeImageRepository extends JpaRepository<CafeImage, Long> {

    @Query("SELECT ci FROM CafeImage ci WHERE ci.cafe.id = :cafeId AND ci.isUsed = true " +
            "ORDER BY CASE WHEN ci.member.id = :memberId THEN 0 ELSE 1 END, " +
            "CASE WHEN ci.member.id = :memberId THEN ci.id END, ci.id")
    Slice<CafeImage> findAllByCafeIdAndIsUsedOrderByCafeImageId(Long cafeId, Long memberId, Pageable pageable);

    List<CafeImage> findAllByMemberId(Long memberId);

    List<CafeImage> findAllByIsUsedFalseAndIsMaskedFalse();
}
