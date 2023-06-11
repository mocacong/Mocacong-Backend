package mocacong.server.domain;

import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CafeImageTest {

    private static final Member member = new Member("kth@apple.com", Platform.APPLE, "1234");

    private static Stream<Arguments> provideCafeImagesMember() {
        Member otherMember = new Member("kth@naver.com", Platform.KAKAO, "1234321");

        return Stream.of(
                Arguments.of(member, true),
                Arguments.of(otherMember, false)
        );
    }

    @ParameterizedTest
    @DisplayName("해당 카페 이미지의 작성자가 해당 회원이 맞는지 여부를 반환한다")
    @MethodSource("provideCafeImagesMember")
    void isOwned(Member input, boolean expected) {
        Cafe cafe = new Cafe("123454321", "케이카페");
        CafeImage cafeImage = new CafeImage("test_url", true, cafe, member);

        assertThat(cafeImage.isOwned(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("해당 카페 이미지 작성자가 없을 경우, 해당 회원이 맞는지 여부 반환은 항상 false 반환한다")
    void isOwnedWhenMemberNull() {
        Cafe cafe = new Cafe("123454321", "케이카페");
        CafeImage cafeImage = new CafeImage("test_url", true, cafe, null);

        assertThat(cafeImage.isOwned(member)).isFalse();
    }

    @Test
    @DisplayName("카페 이미지 작성자를 null 처리한다")
    void removeMember() {
        Cafe cafe = new Cafe("123454321", "케이카페");
        CafeImage cafeImage = new CafeImage("test_url", true, cafe, member);

        cafeImage.removeMember();

        assertThat(cafeImage.getMember()).isNull();
    }
}
