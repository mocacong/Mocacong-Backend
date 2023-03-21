package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidSoundException;

@NoArgsConstructor
@AllArgsConstructor
public enum Sound {

    QUIET("조용해요"),
    NOISY("소음있어요"),
    LOUD("북적북적해요");

    private String value;

    public static Sound from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidSoundException::new);
    }
}
