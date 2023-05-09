package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidToiletException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Toilet {

    CLEAN("깨끗해요", 3),
    NORMAL("평범해요", 2),
    UNCOMFORTABLE("불편해요", 1);

    private String value;
    private int score;

    public static Toilet from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidToiletException::new);
    }

    public static Toilet averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == (int) (score + 0.5))
                .findFirst()
                .orElse(null);
    }
}
