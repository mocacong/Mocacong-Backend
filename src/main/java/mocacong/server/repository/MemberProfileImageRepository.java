package mocacong.server.repository;

import java.util.List;
import mocacong.server.domain.MemberProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileImageRepository extends JpaRepository<MemberProfileImage, Long> {

    List<MemberProfileImage> findAllByIsUsedFalse();
}
