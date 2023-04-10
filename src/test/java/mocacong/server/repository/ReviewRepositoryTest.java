package mocacong.server.repository;

import mocacong.server.config.BaseTimeConfig;
import mocacong.server.domain.*;
import mocacong.server.domain.cafedetail.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BaseTimeConfig.class)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private StudyTypeRepository studyTypeRepository;

    @Test
    @DisplayName("카페 id, 멤버 id로 해당 멤버가 특정 카페에 작성한 리뷰의 id를 조회한다")
    void findIdByCafeIdAndMemberId() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member savedMember = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이", "010-1234-5678"));
        StudyType savedStudyType = studyTypeRepository.save(new StudyType(savedMember, savedCafe, "solo"));
        CafeDetail cafeDetail = new CafeDetail(Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review savedReview = reviewRepository.save(new Review(savedMember, savedCafe, savedStudyType, cafeDetail));

        Long actual = reviewRepository.findIdByCafeIdAndMemberId(savedCafe.getId(), savedMember.getId())
                .orElseThrow();

        assertThat(actual).isEqualTo(savedReview.getId());
    }
}
