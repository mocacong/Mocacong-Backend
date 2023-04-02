package mocacong.server.repository;

import mocacong.server.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByCafeIdAndMemberId(Long cafeId, Long memberId);
}
