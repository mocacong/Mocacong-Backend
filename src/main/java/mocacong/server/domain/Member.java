package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidNicknameException;

import javax.persistence.*;
import java.util.regex.Pattern;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "platform"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {

    private static final Pattern NICKNAME_REGEX = Pattern.compile("^[a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣]{2,6}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @OneToOne
    @JoinColumn(name = "member_profile_image_id")
    private MemberProfileImage memberProfileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform")
    private Platform platform;

    @Column(name = "platform_id")
    private String platformId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "report_count")
    private int reportCount;

    public Member(
            String email, String password, String nickname, MemberProfileImage memberProfileImage,
            Platform platform, String platformId
    ) {
        validateNickname(nickname);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.memberProfileImage = memberProfileImage;
        this.platform = platform;
        this.platformId = platformId;
        this.status = Status.ACTIVE;
    }

    public Member(String email, String password, String nickname, MemberProfileImage memberProfileImage) {
        this(
                email,
                password,
                nickname,
                memberProfileImage,
                Platform.MOCACONG,
                null,
                Status.ACTIVE
        );
    }

    public Member(String email, String password, String nickname) {
        this(email, password, nickname, null, Platform.MOCACONG, null, Status.ACTIVE);
    }

    public Member(String email, Platform platform, String platformId) {
        this.email = email;
        this.platform = platform;
        this.platformId = platformId;
        this.status = Status.ACTIVE;
    }

    public Member(String email, Platform platform, String platformId, Status status) {
        this.email = email;
        this.platform = platform;
        this.platformId = platformId;
        this.status = status;
    }

    public Member(String email, String password, String nickname, MemberProfileImage memberProfileImage, Platform platform, String platformId, Status status) {
        validateNickname(nickname);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.memberProfileImage = memberProfileImage;
        this.platform = platform;
        this.platformId = platformId;
        this.status = status;
    }

    public Member(String email, String password, String nickname, MemberProfileImage memberProfileImage,
                  Platform platform, String platformId, Status status, int reportCount) {
        validateNickname(nickname);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.memberProfileImage = memberProfileImage;
        this.platform = platform;
        this.platformId = platformId;
        this.status = status;
        this.reportCount = reportCount;
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

    public void updateProfileInfo(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_REGEX.matcher(nickname).matches()) {
            throw new InvalidNicknameException();
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public boolean isRegisteredOAuthMember() {
        return nickname != null;
    }

    public void changeStatus(Status status) {
        this.status = status;
        if (status == Status.ACTIVE) {
            resetMemberReportCount();
        }
    }

    public void resetMemberReportCount() {
        this.reportCount = 0;
    }

    public void incrementMemberReportCount() {
        this.reportCount += 1;
        if (this.reportCount >= 11) {
            changeStatus(Status.INACTIVE);
        }
    }
}
