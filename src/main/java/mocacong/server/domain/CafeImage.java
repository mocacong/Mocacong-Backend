package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "cafe_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeImage extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_image_id")
    private Long id;

    @Column(name = "img_url")
    private String imgUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public CafeImage(String imgUrl, Cafe cafe, Member member) {
        this.imgUrl = imgUrl;
        this.cafe = cafe;
        this.member = member;
    }
}
