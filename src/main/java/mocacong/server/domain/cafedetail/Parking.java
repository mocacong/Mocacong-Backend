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

    COMFORTABLE("여유로워요"),
    UNCOMFORTABLE("협소해요"),
    NONE("없어요");

    private String value;

    public static Parking from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidParkingException::new);
    }
}
