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

        CafeDetail cafeDetail1 = new CafeDetail(StudyType.SOLO, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review review1 = new Review(member, cafe, cafeDetail1);
        CafeDetail cafeDetail2 = new CafeDetail(StudyType.GROUP, Wifi.NORMAL, Parking.COMFORTABLE, Toilet.UNCOMFORTABLE, Desk.UNCOMFORTABLE, Power.FEW, Sound.LOUD);
        Review review2 = new Review(member, cafe, cafeDetail2);
        CafeDetail cafeDetail3 = new CafeDetail(StudyType.SOLO, Wifi.NORMAL, Parking.UNCOMFORTABLE, Toilet.NORMAL, Desk.COMFORTABLE, Power.FEW, Sound.LOUD);
        Review review3 = new Review(member, cafe, cafeDetail3);
        cafe.addReview(review1);
        cafe.addReview(review2);
        cafe.addReview(review3);

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertAll(
                () -> assertThat(actual.getStudyType()).isEqualTo(StudyType.SOLO),
                () -> assertThat(actual.getWifi()).isEqualTo(Wifi.NORMAL),
                () -> assertThat(actual.getParking()).isEqualTo(Parking.COMFORTABLE),
                () -> assertThat(actual.getToilet()).isEqualTo(Toilet.NORMAL),
                () -> assertThat(actual.getDesk()).isEqualTo(Desk.NORMAL),
                () -> assertThat(actual.getPower()).isEqualTo(Power.FEW),
                () -> assertThat(actual.getSound()).isEqualTo(Sound.LOUD)
        );
    }

    @Test
    @DisplayName("카페 세부정보 리뷰로 both가 작성될 경우 solo, group 포인트가 모두 1씩 증가한다")
    void updateCafeDetailsWhenStudyTypesAddBoth() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");

        // BOTH 리뷰 추가 -> SOLO, GROUP 모두 1포인트
        CafeDetail cafeDetail1 = new CafeDetail(StudyType.BOTH, Wifi.NORMAL, Parking.COMFORTABLE, Toilet.NORMAL, Desk.UNCOMFORTABLE, Power.FEW, Sound.NOISY);
        Review review1 = new Review(member, cafe, cafeDetail1);
        cafe.addReview(review1);
        cafe.updateCafeDetails();
        CafeDetail actualWhenBothAdd = cafe.getCafeDetail();

        // SOLO 리뷰 추가 -> SOLO 2포인트, GROUP 1포인트
        CafeDetail cafeDetail2 = new CafeDetail(StudyType.SOLO, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review review2 = new Review(member, cafe, cafeDetail2);
        cafe.addReview(review2);
        cafe.updateCafeDetails();
        CafeDetail actualWhenSoloAdd = cafe.getCafeDetail();

        // GROUP 리뷰 추가 -> SOLO, GROUP 모두 2포인트
        CafeDetail cafeDetail3 = new CafeDetail(StudyType.GROUP, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review review3 = new Review(member, cafe, cafeDetail3);
        cafe.addReview(review3);
        cafe.updateCafeDetails();
        CafeDetail actualWhenGroupAdd = cafe.getCafeDetail();

        assertAll(
                () -> assertThat(actualWhenBothAdd.getStudyType()).isEqualTo(StudyType.BOTH),
                () -> assertThat(actualWhenSoloAdd.getStudyType()).isEqualTo(StudyType.SOLO),
                () -> assertThat(actualWhenGroupAdd.getStudyType()).isEqualTo(StudyType.BOTH)
        );
    }

    @Test
    @DisplayName("카페 세부정보 중 study type은 같은 개수일 경우 solo나 group이 아닌 both를 반환한다")
    void updateCafeDetailsWhenStudyTypesEqual() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        Cafe cafe = new Cafe("1", "케이카페");

        CafeDetail cafeDetail1 = new CafeDetail(StudyType.SOLO, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.COMFORTABLE, Power.MANY, Sound.LOUD);
        Review review1 = new Review(member, cafe, cafeDetail1);
        CafeDetail cafeDetail2 = new CafeDetail(StudyType.GROUP, Wifi.NORMAL, Parking.COMFORTABLE, Toilet.NORMAL, Desk.UNCOMFORTABLE, Power.FEW, Sound.NOISY);
        Review review2 = new Review(member, cafe, cafeDetail2);
        cafe.addReview(review1);
        cafe.addReview(review2);

        cafe.updateCafeDetails();

        CafeDetail actual = cafe.getCafeDetail();
        assertThat(actual.getStudyType()).isEqualTo(StudyType.BOTH);
    }

    @Test
    @DisplayName("카페에 일부 세부정보 리뷰가 하나도 없을 경우 해당 세부정보는 null을 반환한다")
    void updateCafeDetailsWhenSomeTypesNoReviews() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        Cafe cafe = new Cafe("1", "케이카페");
        CafeDetail cafeDetail = new CafeDetail(StudyType.SOLO, Wifi.FAST, null, Toilet.CLEAN, null, Power.MANY, Sound.LOUD);

        Review review = new Review(member, cafe, cafeDetail);
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
