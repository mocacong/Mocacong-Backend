package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.CafeDetail;
import mocacong.server.domain.Member;
import mocacong.server.domain.Review;
import mocacong.server.domain.cafedetail.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("카페 id, 멤버 id로 해당 멤버가 특정 카페에 작성한 리뷰의 id를 조회한다")
    void findIdByCafeIdAndMemberId() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member savedMember = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));
        CafeDetail cafeDetail = new CafeDetail(StudyType.SOLO, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review savedReview = reviewRepository.save(new Review(savedMember, savedCafe, cafeDetail));

        Long actual = reviewRepository.findIdByCafeIdAndMemberId(savedCafe.getId(), savedMember.getId())
                .orElseThrow();

        assertThat(actual).isEqualTo(savedReview.getId());
    }
}
