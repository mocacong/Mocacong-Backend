package mocacong.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
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
}
