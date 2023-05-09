package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidParkingException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Parking {

    COMFORTABLE("여유로워요", 3),
    UNCOMFORTABLE("협소해요", 2),
    NONE("없어요", 1);

    private String value;
    private int score;

    public static Parking from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidParkingException::new);
    }

    public static Parking averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == (int) (score + 0.5))
                .findFirst()
                .orElse(null);
    }
}
