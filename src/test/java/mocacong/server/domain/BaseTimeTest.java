package mocacong.server.domain;

import java.sql.Timestamp;
import mocacong.server.domain.cafedetail.*;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.RepositoryTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@RepositoryTest
class BaseTimeTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CafeRepository cafeRepository;
    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("멤버를 저장하면 생성 시각이 자동으로 저장된다")
    public void memberCreatedAtNow() {
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리");

        memberRepository.save(member);

        assertThat(member.getCreatedTime()).isNotNull();
    }

    @Test
    @DisplayName("카페 객체를 수정하면 수정 시각이 자동으로 저장된다")
    public void updateCafeAtNow() {
        Member member = new Member("kth990303@naver.com", "a1b2c3d4", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        CafeDetail cafeDetail = new CafeDetail(StudyType.SOLO, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.UNCOMFORTABLE, Power.MANY, Sound.LOUD);
        Review addReview = new Review(member, cafe, cafeDetail);
        cafe.addReview(addReview);
        cafe.updateCafeDetails();
        entityManager.flush();

        Cafe findCafe = cafeRepository.findByMapId("2143154352323").orElseThrow(NotFoundCafeException::new);
        Timestamp createdTime = findCafe.getCreatedTime();
        Timestamp modifiedTime = findCafe.getModifiedTime();

        assertAll(
                () -> assertThat(modifiedTime).isNotNull(),
                () -> assertThat(createdTime).isNotNull(),
                () -> assertThat(modifiedTime).isAfter(createdTime)
        );
    }
}
