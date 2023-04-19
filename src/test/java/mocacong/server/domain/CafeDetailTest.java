package mocacong.server.domain;

import mocacong.server.domain.cafedetail.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CafeDetailTest {

    @Test
    @DisplayName("카페 세부정보가 존재하면 해당 값의 value를, 존재하지 않으면 null을 반환한다")
    void getTypeValue() {
        CafeDetail actual = new CafeDetail(StudyType.SOLO, Wifi.FAST, null, Toilet.CLEAN, null, Power.MANY, Sound.LOUD);

        assertAll(
                () -> assertThat(actual.getWifiValue()).isEqualTo(Wifi.FAST.getValue()),
                () -> assertThat(actual.getParkingValue()).isNull(),
                () -> assertThat(actual.getToiletValue()).isEqualTo(Toilet.CLEAN.getValue()),
                () -> assertThat(actual.getDeskValue()).isNull(),
                () -> assertThat(actual.getPowerValue()).isEqualTo(Power.MANY.getValue()),
                () -> assertThat(actual.getSoundValue()).isEqualTo(Sound.LOUD.getValue())
        );
    }
}
