package mocacong.server.domain;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoordinateTest {


    @Test
    @DisplayName("x, y 좌표가 모두 같아야 같은 위치로 처리된다")
    void sameCoordinate() {
        Coordinate coordinate1 = new Coordinate(new BigDecimal("37.5666805"), new BigDecimal("126.9784147"));
        Coordinate coordinate2 = new Coordinate(new BigDecimal("37.5666805"), new BigDecimal("126.9784147"));

        assertThat(coordinate1).isEqualTo(coordinate2);
    }

    @Test
    @DisplayName("x, y 좌표 중 하나라도 다르면 다른 위치로 처리된다")
    void differentCoordinate() {
        Coordinate coordinate1 = new Coordinate(new BigDecimal("37.5666805"), new BigDecimal("126.9784147"));
        Coordinate coordinate2 = new Coordinate(new BigDecimal("37.5666804"), new BigDecimal("126.9784147"));

        assertThat(coordinate1).isNotEqualTo(coordinate2);
    }
}
