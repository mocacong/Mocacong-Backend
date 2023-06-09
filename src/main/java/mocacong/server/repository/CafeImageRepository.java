package mocacong.server.repository;

import mocacong.server.domain.CafeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeImageRepository extends JpaRepository<CafeImage, Long> {

    List<CafeImage> findAllByCafeIdAndIsUsedTrue(Long cafeId);

    List<CafeImage> findAllByIsUsedFalse();
}
