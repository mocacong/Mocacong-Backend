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

    CLEAN("깨끗해요"),
    NORMAL("평범해요"),
    UNCOMFORTABLE("불편해요");

    private String value;

    public static Toilet from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidToiletException::new);
    }
}
