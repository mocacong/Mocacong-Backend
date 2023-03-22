package mocacong.server.domain;

import mocacong.server.domain.cafedetail.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CafeTest {

    @Test
    @DisplayName("카페에 평점을 기여한 사람이 없으면 0점을 반환한다")
    void findScoreWithNoReviews() {
        Cafe cafe = new Cafe("케이카페");

        double actual = cafe.findAverageScore();

        assertThat(actual).isEqualTo(0.0);
    }

    @Test
    @DisplayName("카페의 평점을 올바르게 계산하여 반환한다")
    void findScore() {
        Member member = new Member("kth@naver.com", "1234", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("케이카페");
        Score score1 = new Score(5, member, cafe);
        Score score2 = new Score(2, member, cafe);

        double actual = cafe.findAverageScore();

        assertThat(actual).isEqualTo(3.5);
    }

    @Test
    @DisplayName("카페 세부정보 갱신이 올바르게 작동한다")
    void updateCafeDetails() {
        Member member = new Member("kth@naver.com", "1234", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("케이카페");
        CafeDetail cafeDetail1 = new CafeDetail(StudyType.GROUP, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD, Tumbler.NO_SALE);
        Review review1 = new Review(member, cafe, cafeDetail1);
        CafeDetail cafeDetail2 = new CafeDetail(StudyType.SOLO, Wifi.SLOW, Parking.COMFORTABLE, Toilet.DIRTY, Desk.UNCOMFORTABLE, Power.FEW, Sound.NOISY, Tumbler.NO_SALE);
        Review review2 = new Review(member, cafe, cafeDetail2);
        CafeDetail cafeDetail3 = new CafeDetail(StudyType.SOLO, Wifi.SLOW, Parking.UNCOMFORTABLE, Toilet.DIRTY, Desk.COMFORTABLE, Power.FEW, Sound.LOUD, Tumbler.NO_SALE);
        Review review3 = new Review(member, cafe, cafeDetail3);
        cafe.addReview(review1);
        cafe.addReview(review2);
        cafe.addReview(review3);

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertAll(
                () -> assertThat(actual.getStudyType()).isEqualTo(StudyType.SOLO),
                () -> assertThat(actual.getWifi()).isEqualTo(Wifi.SLOW),
                () -> assertThat(actual.getParking()).isEqualTo(Parking.COMFORTABLE),
                () -> assertThat(actual.getToilet()).isEqualTo(Toilet.DIRTY),
                () -> assertThat(actual.getDesk()).isEqualTo(Desk.COMFORTABLE),
                () -> assertThat(actual.getPower()).isEqualTo(Power.FEW),
                () -> assertThat(actual.getSound()).isEqualTo(Sound.LOUD),
                () -> assertThat(actual.getTumbler()).isEqualTo(Tumbler.NO_SALE)
        );
    }
}
