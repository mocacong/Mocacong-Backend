package mocacong.server.domain.cafedetail;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidStudyTypeException;

@NoArgsConstructor
@AllArgsConstructor
public enum StudyType {

    SOLO("혼자"),
    GROUP("여럿");

    private String value;

    public static StudyType from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidStudyTypeException::new);
    }
}
