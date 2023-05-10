package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidDeskException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Desk {

    COMFORTABLE("편해요", 3),
    NORMAL("보통이에요", 2),
    UNCOMFORTABLE("불편해요", 1);

    private String value;
    private int score;

    public static Desk from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidDeskException::new);
    }

    public static Desk averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == Math.round(score))
                .findFirst()
                .orElse(null);
    }
}
