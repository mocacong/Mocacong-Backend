package mocacong.server.repository;

import java.util.Optional;
import mocacong.server.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByCafeIdAndMemberId(Long cafeId, Long memberId);
}
