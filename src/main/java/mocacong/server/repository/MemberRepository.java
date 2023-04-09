package mocacong.server.repository;

import java.util.Optional;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    Optional<Long> findIdByPlatformAndPlatformId(Platform platform, String platformId);
}
