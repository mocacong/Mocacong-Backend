package mocacong.server.repository;

import mocacong.server.domain.CafeImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CafeImageRepository extends JpaRepository<CafeImage, Long> {

    @Query("SELECT ci FROM CafeImage ci WHERE ci.cafe.id = :cafeId AND ci.isUsed = true ORDER BY ci.id DESC")
    List<CafeImage> findAllByCafeIdAndIsUsedOrderByCafeImageIdDesc(Long cafeId);

    @Query("SELECT ci FROM CafeImage ci WHERE ci.cafe.id = :cafeId AND ci.isUsed = true ORDER BY ci.id DESC")
    Slice<CafeImage> findAllByCafeIdAndIsUsedOrderByCafeImageIdDesc(Long cafeId, Pageable pageable);

    List<CafeImage> findAllByIsUsedFalse();
}
