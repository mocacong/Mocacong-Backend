package mocacong.server.repository;

import feign.Param;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPlatformAndPlatformId(Platform platform, String platformId);

    Optional<Member> findByEmailAndPlatform(String email, Platform platform);

    Optional<List<Member>> findByStatus(Status status);

    Boolean existsByEmailAndPlatform(String email, Platform platform);

    Boolean existsByNickname(String nickname);

    @Query("select m.id from Member m where m.platform = :platform and m.platformId = :platformId")
    Optional<Long> findIdByPlatformAndPlatformId(Platform platform, String platformId);

    @Modifying
    @Query("UPDATE Member m SET m.status = :newStatus WHERE m.status = :oldStatus AND " +
            "m.modifiedTime <= :thresholdDateTime")
    void bulkUpdateStatus(@Param("newStatus") Status newStatus, @Param("oldStatus") Status oldStatus,
                          @Param("thresholdDateTime") Date thresholdDateTime);
}
