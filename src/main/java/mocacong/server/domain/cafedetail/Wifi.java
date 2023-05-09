package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidWifiException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Wifi {

    FAST("빵빵해요", 3),
    NORMAL("적당해요", 2),
    SLOW("느려요", 1);

    private String value;
    private int score;

    public static Wifi from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidWifiException::new);
    }

    public static Wifi averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == (int) (score + 0.5))
                .findFirst()
                .orElse(null);
    }
}
