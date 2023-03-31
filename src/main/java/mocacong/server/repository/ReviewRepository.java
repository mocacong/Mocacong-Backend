package mocacong.server.repository;

import java.util.Optional;
import mocacong.server.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select r.id " +
            "from Review r " +
            "join r.cafe c " +
            "join r.member m " +
            "where c.id = :cafeId and m.id = :memberId")
    Optional<Long> findIdByCafeIdAndMemberId(Long cafeId, Long memberId);
}
