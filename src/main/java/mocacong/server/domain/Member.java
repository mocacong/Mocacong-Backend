package mocacong.server.domain;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

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
        // TODO: 검증 로직 프론트와 회의 후 작성하기
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
    }
}
