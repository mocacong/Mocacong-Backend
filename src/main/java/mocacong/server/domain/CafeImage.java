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

    @Column(name = "img_url", unique = true)
    private String imgUrl;

    @Column(name = "is_used")
    private Boolean isUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public CafeImage(String imgUrl, Boolean isUsed, Cafe cafe, Member member) {
        this.imgUrl = imgUrl;
        this.isUsed = isUsed;
        this.cafe = cafe;
        this.member = member;
    }

    public boolean isOwned(Member member) {
        return this.member.equals(member);
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }
}
