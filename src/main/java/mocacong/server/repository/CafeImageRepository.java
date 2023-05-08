package mocacong.server.repository;

import mocacong.server.domain.CafeImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CafeImageRepository extends JpaRepository<CafeImage, Long> {
    Slice<CafeImage> findAllByCafeIdAndIsUsedTrue(Long cafeId, Pageable pageRequest);
}
