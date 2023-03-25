package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidPowerException;

@NoArgsConstructor
@AllArgsConstructor
public enum Power {

    MANY("충분해요"),
    FEW("적당해요"),
    NONE("없어요");

    private String value;

    public static Power from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidPowerException::new);
    }
}
