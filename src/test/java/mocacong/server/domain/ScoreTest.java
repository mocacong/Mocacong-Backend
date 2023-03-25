package mocacong.server.domain;

import java.math.BigDecimal;
import mocacong.server.exception.badrequest.InvalidScoreException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScoreTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("평점이 1점 이상 5점 이하가 아니면 예외를 반환한다")
    void invalidRangeScore(int score) {
        Member member = new Member("kth@naver.com", "1234", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("케이카페", new BigDecimal("37.5666805"), new BigDecimal("126.9784147"));

        assertThatThrownBy(() -> new Score(score, member, cafe))
                .isInstanceOf(InvalidScoreException.class);
    }
}
