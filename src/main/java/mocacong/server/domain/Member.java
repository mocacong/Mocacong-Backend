package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidNicknameException;
import mocacong.server.exception.badrequest.InvalidPhoneException;

import javax.persistence.*;
import java.util.regex.Pattern;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    private static final Pattern NICKNAME_REGEX = Pattern.compile("^[a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣]{2,6}$");
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

    @Column(name = "img_url")
    private String imgUrl;

    public Member(String email, String password, String nickname, String phone, String imgUrl) {
        validateMemberInfo(nickname, phone);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.imgUrl = imgUrl;
    }

    public Member(String email, String password, String nickname, String phone) {
        this(email, password, nickname, phone, null);
    }

    private void validateMemberInfo(String nickname, String phone) {
        validateNickname(nickname);
        validatePhone(phone);
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

    public void updateProfileImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
