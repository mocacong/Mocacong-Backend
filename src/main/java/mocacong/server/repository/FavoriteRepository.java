package mocacong.server.repository;

import java.util.List;
import java.util.Optional;
import mocacong.server.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("select f.id " +
            "from Favorite f " +
            "join f.cafe c " +
            "join f.member m " +
            "where c.id = :cafeId and m.id = :memberId")
    Optional<Long> findFavoriteIdByCafeIdAndMemberId(Long cafeId, Long memberId);

    List<Favorite> findAllByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Favorite f where f.member.id IS null")
    void deleteAllByMemberIdIsNull();
}
