package mocacong.server.repository;

import java.util.Optional;
import mocacong.server.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByCafeIdAndMemberId(Long cafeId, Long memberId);
    boolean existsByCafeIdAndMemberId(Long cafeId, Long memberId);
}
