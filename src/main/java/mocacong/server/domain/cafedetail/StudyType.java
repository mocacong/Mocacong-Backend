package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidStudyTypeException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum StudyType {

    SOLO("solo"),
    GROUP("group"),
    BOTH("both");

    private String value;

    public static StudyType from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidStudyTypeException::new);
    }
}
