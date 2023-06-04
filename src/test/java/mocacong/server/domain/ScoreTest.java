package mocacong.server.domain;

import mocacong.server.exception.badrequest.InvalidScoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoreTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("평점이 1점 이상 5점 이하가 아니면 예외를 반환한다")
    void invalidRangeScore(int score) {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");

        assertThatThrownBy(() -> new Score(score, member, cafe))
                .isInstanceOf(InvalidScoreException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("수정 시 평점이 1점 이상 5점 이하가 아니면 예외를 반환한다")
    void updateInvalidRangeScore(int score) {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        Score oldScore = new Score(3, member, cafe);
        assertThatThrownBy(() -> oldScore.updateScore(score))
                .isInstanceOf(InvalidScoreException.class);
    }
}
