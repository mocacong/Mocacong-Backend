package mocacong.server.domain;

import mocacong.server.domain.cafedetail.*;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class BaseTimeTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CafeRepository cafeRepository;

    @Test
    @DisplayName("멤버를 저장하면 생성 시각이 자동으로 저장된다")
    public void memberCreatedAtNow() {
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");

        memberRepository.save(member);

        assertThat(member.getCreatedTime()).isNotNull();
    }

    @Test
    @DisplayName("카페 리뷰를 수정하면 수정 시각이 자동으로 저장된다")
    public void updateCafeAtNow() {
        Member member = new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        StudyType studyType = new StudyType(member, cafe, "solo");
        CafeDetail cafeDetail = new CafeDetail(Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.UNCOMFORTABLE, Power.MANY, Sound.LOUD);
        Review addReview = new Review(member, cafe, studyType, cafeDetail);
        cafe.addReview(addReview);
        CafeDetail changedCafeDetail = new CafeDetail(Wifi.NORMAL, Parking.NONE, Toilet.CLEAN, Desk.NORMAL, Power.MANY, Sound.LOUD);
        Review updateReview = new Review(member, cafe, studyType, changedCafeDetail);
        cafe.addReview(updateReview);

        updateReview.updateReview(changedCafeDetail);
        cafe.updateCafeDetails();
        LocalDateTime modifiedTime = cafe.getModifiedTime();

        assertThat(modifiedTime).isNotNull();
    }
}
