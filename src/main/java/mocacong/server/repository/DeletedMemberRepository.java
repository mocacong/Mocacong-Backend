package mocacong.server.repository;

import mocacong.server.domain.DeletedMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeletedMember dm where dm.createdTime <= :thresholdDateTime")
    void deleteDeletedMemberByCreatedTime(Date thresholdDateTime);
}
