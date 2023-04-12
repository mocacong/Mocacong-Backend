package mocacong.server.domain;

import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPhoneException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberTest {

    @Test
    @DisplayName("모든 정보를 올바른 형식으로 입력하면 회원이 생성된다")
    void createMember() {
        assertDoesNotThrow(
                () -> new Member("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678")
        );
    }

    @Test
    @DisplayName("OAuth 회원 가입 시에 입력받은 정보로 수정한다")
    void registerOAuthMember() {
        Member member = new Member("kth@apple.com", Platform.APPLE, "1234321");

        member.registerOAuthMember("kth990303@apple.com", "케이");

        assertAll(
                () -> assertThat(member.getEmail()).isEqualTo("kth990303@apple.com"),
                () -> assertThat(member.getNickname()).isEqualTo("케이")
        );
    }

    @Test
    @DisplayName("OAuth 회원 가입 시에 입력받은 정보 중 이메일이 null이면 이메일은 수정되지 않는다")
    void registerOAuthMemberWhenEmailIsNull() {
        Member member = new Member("kth@apple.com", Platform.APPLE, "1234321");

        member.registerOAuthMember(null, "케이");

        assertAll(
                () -> assertThat(member.getEmail()).isNotNull(),
                () -> assertThat(member.getNickname()).isEqualTo("케이")
        );
    }

    @ParameterizedTest
    @DisplayName("닉네임은 초성, 중성, 종성 분리하여 지을 수 있다")
    @ValueSource(strings = {"ㄱㅁㄴㄷㄹ", "ㅏㅣㅓㅜ", "가ㅏ누ㅟ"})
    void createMemberNicknameOnlyOnset(String nickname) {
        assertDoesNotThrow(
                () -> new Member("kth990303@naver.com", "a1b2c3d4", nickname, "010-1234-5678")
        );
    }

    @ParameterizedTest
    @DisplayName("닉네임이 2~6자가 아니면 예외를 반환한다")
    @ValueSource(strings = {"일", "일이삼사오육칠"})
    void nicknameLengthValidation(String nickname) {
        assertThatThrownBy(() -> new Member("kth990303@naver.com", "a1b2c3d4", nickname, "010-1234-5678"))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @ParameterizedTest
    @DisplayName("닉네임이 영어 또는 한글이 아니면 예외를 반환한다")
    @ValueSource(strings = {"123", "愛してるよ"})
    void nicknameConfigureValidation(String nickname) {
        assertThatThrownBy(() -> new Member("kth990303@naver.com", "a1b2c3d4", nickname, "010-1234-5678"))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @ParameterizedTest
    @DisplayName("전화번호가 01로 시작하지 않거나 하이픈 포함 10~14자가 아니면 예외를 반환한다")
    @ValueSource(strings = {"030-1234-5678", "01-123-45", "010-12345-56789", "010123456", "012345678901234"})
    void phoneValidation(String phone) {
        assertThatThrownBy(() -> new Member("kth990303@naver.com", "a1b2c3d4", "케이", phone))
                .isInstanceOf(InvalidPhoneException.class);
    }
}
