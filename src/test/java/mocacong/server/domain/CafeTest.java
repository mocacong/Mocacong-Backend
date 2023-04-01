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
        Cafe cafe = new Cafe("1", "케이카페");

        double actual = cafe.findAverageScore();

        assertThat(actual).isEqualTo(0.0);
    }

    @Test
    @DisplayName("카페의 평점을 올바르게 계산하여 반환한다")
    void findScore() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        Score score1 = new Score(5, member, cafe);
        Score score2 = new Score(2, member, cafe);

        double actual = cafe.findAverageScore();

        assertThat(actual).isEqualTo(3.5);
    }

    @Test
    @DisplayName("카페 세부정보 갱신이 올바르게 작동한다")
    void updateCafeDetails() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        Cafe cafe = new Cafe("1", "케이카페");
        StudyType studyType = new StudyType(member, cafe, "solo");

        CafeDetail cafeDetail1 = new CafeDetail(Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review review1 = new Review(member, cafe, studyType, cafeDetail1);
        CafeDetail cafeDetail2 = new CafeDetail(Wifi.NORMAL, Parking.COMFORTABLE, Toilet.NORMAL, Desk.UNCOMFORTABLE, Power.FEW, Sound.NOISY);
        Review review2 = new Review(member, cafe, studyType, cafeDetail2);
        CafeDetail cafeDetail3 = new CafeDetail(Wifi.NORMAL, Parking.UNCOMFORTABLE, Toilet.NORMAL, Desk.COMFORTABLE, Power.FEW, Sound.LOUD);
        Review review3 = new Review(member, cafe, studyType, cafeDetail3);
        cafe.addReview(review1);
        cafe.addReview(review2);
        cafe.addReview(review3);

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertAll(
                () -> assertThat(actual.getWifi()).isEqualTo(Wifi.NORMAL),
                () -> assertThat(actual.getParking()).isEqualTo(Parking.COMFORTABLE),
                () -> assertThat(actual.getToilet()).isEqualTo(Toilet.NORMAL),
                () -> assertThat(actual.getDesk()).isEqualTo(Desk.COMFORTABLE),
                () -> assertThat(actual.getPower()).isEqualTo(Power.FEW),
                () -> assertThat(actual.getSound()).isEqualTo(Sound.LOUD)
        );
    }

    @Test
    @DisplayName("카페에 일부 세부정보 리뷰가 하나도 없을 경우 해당 세부정보는 null을 반환한다")
    void updateCafeDetailsWhenSomeTypesNoReviews() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        Cafe cafe = new Cafe("1", "케이카페");
        StudyType studyType = new StudyType(member, cafe, "solo");
        CafeDetail cafeDetail = new CafeDetail(Wifi.FAST, null, Toilet.CLEAN, null, Power.MANY, Sound.LOUD);

        Review review = new Review(member, cafe, studyType, cafeDetail);
        cafe.addReview(review);

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertThat(actual.getParking()).isNull();
    }

    @Test
    @DisplayName("카페에 리뷰가 하나도 없을 경우 모든 세부정보 타입에 null을 반환한다")
    void updateCafeDetailsWhenNoReviews() {
        Cafe cafe = new Cafe("1", "케이카페");

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertAll(
                () -> assertThat(actual.getWifi()).isNull(),
                () -> assertThat(actual.getParking()).isNull(),
                () -> assertThat(actual.getToilet()).isNull(),
                () -> assertThat(actual.getDesk()).isNull(),
                () -> assertThat(actual.getPower()).isNull(),
                () -> assertThat(actual.getSound()).isNull()
        );
    }

}
