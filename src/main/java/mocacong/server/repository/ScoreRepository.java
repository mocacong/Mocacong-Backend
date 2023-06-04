package mocacong.server.repository;

import mocacong.server.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByCafeIdAndMemberId(Long cafeId, Long memberId);

    List<Score> findAllByMemberId(Long memberId);

    @Query("select s.score from Score s " +
            "join s.cafe c " +
            "join s.member m " +
            "where c.id = :cafeId and m.id = :memberId")
    int findScoreByCafeIdAndMemberId(Long cafeId, Long memberId);
}
