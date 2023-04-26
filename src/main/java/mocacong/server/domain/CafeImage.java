package mocacong.server.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@NoArgsConstructor
@Getter
public class CafeImage {

    @Column(name = "member")
    @Enumerated(EnumType.STRING)
    private Member member;

    @Column(name = "img_url")
    @Enumerated(EnumType.STRING)
    private String imgUrl;

    public CafeImage(Member member, String imgUrl) {
        this.member = member;
        this.imgUrl = imgUrl;
    }
}
