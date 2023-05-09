package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidSoundException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Sound {

    QUIET("조용해요", 3),
    NOISY("적당해요", 2),
    LOUD("북적북적해요", 1);

    private String value;
    private int score;

    public static Sound from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidSoundException::new);
    }

    public static Sound averageFrom(double score) {
        return Arrays.stream(values())
                .filter(it -> it.score == (int) (score + 0.5))
                .findFirst()
                .orElse(null);
    }
}
