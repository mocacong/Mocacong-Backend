package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidWifiException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Wifi {

    FAST("빵빵해요"),
    NORMAL("적당해요"),
    SLOW("느려요");

    private String value;

    public static Wifi from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidWifiException::new);
    }
}
