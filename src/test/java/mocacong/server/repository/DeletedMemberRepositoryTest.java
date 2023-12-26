package mocacong.server.repository;

import mocacong.server.domain.DeletedMember;
import mocacong.server.domain.Member;
import mocacong.server.domain.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RepositoryTest
class DeletedMemberRepositoryTest {

    @Autowired
    private DeletedMemberRepository deletedMemberRepository;

    @Test
    @DisplayName("회원이 삭제된지 thresholdDateTime만큼 지났으면 모두 삭제한다.")
    void deleteDeletedMemberByCreatedTime() {
        DeletedMember member1 = new DeletedMember("dlawotn3@naver.com", "password1", "mery");
        DeletedMember member2 = new DeletedMember("dlawotn2@naver.com", "password2", "케이");
        deletedMemberRepository.save(member1);
        deletedMemberRepository.save(member2);

        LocalDate thresholdDateTime = LocalDate.now().plusDays(1);
        Instant instant = thresholdDateTime.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date thresholdDate = Date.from(instant);
        deletedMemberRepository.deleteDeletedMemberByCreatedTime(thresholdDate);


        List<DeletedMember> actual = deletedMemberRepository.findAll();

        assertThat(actual).hasSize(0);
    }
}