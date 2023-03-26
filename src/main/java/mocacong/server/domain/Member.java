package mocacong.server.domain;

import java.util.regex.Pattern;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPasswordException;
import mocacong.server.exception.badrequest.InvalidPhoneException;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,20}$");
    private static final Pattern NICKNAME_REGEX = Pattern.compile("^[a-zA-Z가-힣]{2,6}$");
    private static final Pattern PHONE_REGEX = Pattern.compile("^01[\\d\\-]{8,12}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "phone", nullable = false)
    private String phone;

    public Member(String email, String password, String nickname, String phone) {
        validateMemberInfo(password, nickname, phone);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
    }

    private void validateMemberInfo(String password, String nickname, String phone) {
        validatePassword(password);
        validateNickname(nickname);
        validatePhone(phone);
    }

    private void validatePassword(String password) {
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            throw new InvalidPasswordException();
        }
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_REGEX.matcher(nickname).matches()) {
            throw new InvalidNicknameException();
        }
    }

    private void validatePhone(String phone) {
        if (!PHONE_REGEX.matcher(phone).matches()) {
            throw new InvalidPhoneException();
        }
    }
}
