package mocacong.server.domain;

import mocacong.server.exception.badrequest.InvalidStudyTypeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudyTypeTest {

    @Test
    @DisplayName("타입에는 solo, group 외의 정보가 들어갈 수 없다")
    void studyTypeValue() {
        Member member = new Member("kth@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        Cafe cafe = new Cafe("1", "케이카페");
        assertThatThrownBy(() -> new StudyType(member, cafe, "invalid"))
                .isInstanceOf(InvalidStudyTypeException.class);
    }
}
