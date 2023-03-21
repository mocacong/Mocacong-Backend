package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidToiletException;

@NoArgsConstructor
@AllArgsConstructor
public enum Toilet {

    CLEAN("깨끗해요"),
    DIRTY("더러워요"),
    NONE("없어요");

    private String value;

    public static Toilet from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidToiletException::new);
    }
}
