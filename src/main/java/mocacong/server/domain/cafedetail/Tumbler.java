package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidTumblerException;

@NoArgsConstructor
@AllArgsConstructor
public enum Tumbler {

    SALE("있어요"),
    NO_SALE("없어요");

    private String value;

    public static Tumbler from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidTumblerException::new);
    }
}
