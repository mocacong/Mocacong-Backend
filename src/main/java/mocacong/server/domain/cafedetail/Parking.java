package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidParkingException;

@NoArgsConstructor
@AllArgsConstructor
public enum Parking {

    COMFORTABLE("편해요"),
    UNCOMFORTABLE("불편해요"),
    NONE("없어요");

    private String value;

    public static Parking from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidParkingException::new);
    }
}
