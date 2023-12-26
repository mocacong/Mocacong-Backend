package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "deleted_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedMember extends BaseTime{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deleted_member_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

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

    private DeletedMember(String email, String password, String nickname, Platform platform, String platformId, Status status, int reportCount) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.platform = platform;
        this.platformId = platformId;
        this.status = status;
        this.reportCount = reportCount;
    }

    public DeletedMember(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public static DeletedMember from(Member member) {
        return new DeletedMember(
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                member.getPlatform(),
                member.getPlatformId(),
                member.getStatus(),
                member.getReportCount()
        );
    }
}
