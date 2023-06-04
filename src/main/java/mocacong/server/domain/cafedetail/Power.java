package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidPowerException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Power {

    MANY("충분해요", 3),
    FEW("적당해요", 2),
    NONE("없어요", 1);

    private String value;
    private int score;

    public static Power from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidPowerException::new);
    }

    public static Power averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == Math.round(score))
                .findFirst()
                .orElse(null);
    }
}
