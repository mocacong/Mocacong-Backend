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
public class Member extends BaseTime {

    private static final Pattern NICKNAME_REGEX = Pattern.compile("^[a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣]{2,6}$");
    private static final Pattern PHONE_REGEX = Pattern.compile("^01[\\d\\-]{8,12}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "phone")
    private String phone;

    @OneToOne
    @JoinColumn(name = "member_profile_image_id")
    private MemberProfileImage memberProfileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform")
    private Platform platform;

    @Column(name = "platform_id")
    private String platformId;

    public Member(
            String email, String password, String nickname, String phone, MemberProfileImage memberProfileImage,
            Platform platform, String platformId
    ) {
        validateMemberInfo(nickname, phone);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.memberProfileImage = memberProfileImage;
        this.platform = platform;
        this.platformId = platformId;
    }

    public Member(String email, String password, String nickname, String phone, MemberProfileImage memberProfileImage) {
        this(
                email,
                password,
                nickname,
                phone,
                memberProfileImage,
                Platform.MOCACONG,
                null
        );
    }

    public Member(String email, String password, String nickname, String phone) {
        this(email, password, nickname, phone, null, Platform.MOCACONG, null);
    }

    public Member(String email, Platform platform, String platformId) {
        this.email = email;
        this.platform = platform;
        this.platformId = platformId;
    }

    public void registerOAuthMember(String email, String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
        if (email != null) {
            this.email = email;
        }
    }

    public String getImgUrl() {
        return this.memberProfileImage != null ? this.memberProfileImage.getImgUrl() : null;
    }

    public void updateProfileImgUrl(MemberProfileImage memberProfileImage) {
        updateBeforeProfileImageNotUsedStatus();
        this.memberProfileImage = memberProfileImage;
    }

    private void updateBeforeProfileImageNotUsedStatus() {
        if (this.memberProfileImage != null) {
            this.memberProfileImage.updateNotUsedStatus();
        }
    }

    public void updateProfileInfo(String nickname, String phone) {
        validateMemberInfo(nickname, phone);
        this.nickname = nickname;
        this.phone = phone;
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

    public void updatePassword(String password) {
        this.password = password;
    }

    public boolean isRegisteredOAuthMember() {
        return nickname != null;
    }
}
