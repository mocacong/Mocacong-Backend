package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "favorite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTime {

    @Id
    @Column(name = "favorite_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    public Favorite(Member member, Cafe cafe) {
        this.member = member;
        this.cafe = cafe;
    }
}
