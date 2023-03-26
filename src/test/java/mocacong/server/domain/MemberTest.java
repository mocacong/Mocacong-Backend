package mocacong.server.domain;

import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.badrequest.InvalidPhoneException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @ParameterizedTest
    @DisplayName("비밀번호가 8~20자가 아니면 예외를 반환한다")
    @ValueSource(strings = {"abcdef7", "abcdefgabcdefgabcde21"})
    void passwordLengthValidation(String password) {
        assertThatThrownBy(() -> new Member("kth990303@naver.com", password, "케이", "010-1234-5678"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 소문자, 숫자를 모두 포함하지 않으면 예외를 반환한다")
    @ValueSource(strings = {"abcdefgh", "12345678"})
    void passwordConfigureValidation(String password) {
        assertThatThrownBy(() -> new Member("kth990303@naver.com", password, "케이", "010-1234-5678"))
                .isInstanceOf(InvalidPasswordException.class);
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
