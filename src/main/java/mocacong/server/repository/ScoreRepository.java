package mocacong.server.repository;

import java.util.Optional;
import mocacong.server.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByCafeIdAndMemberId(Long cafeId, Long memberId);
}
