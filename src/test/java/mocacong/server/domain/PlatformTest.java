package mocacong.server.domain;

import mocacong.server.exception.badrequest.InvalidPlatformException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlatformTest {

    @Test
    @DisplayName("요청으로 들어온 platform을 올바르게 반환한다")
    void from() {
        Platform expected = Platform.APPLE;

        Platform actual = Platform.from("apple");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("요청으로 들어온 platform이 올바르지 않으면 예외를 반환한다")
    void fromInvalid() {
        Platform expected = Platform.MOCACONG;

        assertThatThrownBy(() -> Platform.from(null))
                .isInstanceOf(InvalidPlatformException.class);
    }
}
