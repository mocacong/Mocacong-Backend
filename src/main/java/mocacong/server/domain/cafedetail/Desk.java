package mocacong.server.domain.cafedetail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidDeskException;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Desk {

    COMFORTABLE("편해요"),
    NORMAL("보통이에요"),
    UNCOMFORTABLE("불편해요");

    private String value;

    public static Desk from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidDeskException::new);
    }
}
