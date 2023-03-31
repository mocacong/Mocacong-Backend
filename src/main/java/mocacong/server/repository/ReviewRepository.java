package mocacong.server.repository;

import mocacong.server.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select r.id " +
            "from Review r " +
            "join r.cafe c " +
            "join r.member m " +
            "where c.id = :cafeId and m.id = :memberId")
    Optional<Long> findIdByCafeIdAndMemberId(Long cafeId, Long memberId);
<<<<<<< HEAD

    Review findByCafeIdAndMemberId(Long id, Long id1);
=======
>>>>>>> 91436bb (feat: 카페 세부정보 수정 비즈니스 로직 구현)
}
