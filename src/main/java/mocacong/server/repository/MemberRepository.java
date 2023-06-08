package mocacong.server.repository;

import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByPlatformAndPlatformId(Platform platform, String platformId);

    Boolean existsByEmailAndPlatform(String email, Platform platform);

    @Query("select m.id from Member m where m.platform = :platform and m.platformId = :platformId")
    Optional<Long> findIdByPlatformAndPlatformId(Platform platform, String platformId);
}
