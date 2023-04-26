package mocacong.server.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@Getter
public class CafeImage {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "img_url")
    private String imgUrl;

    public CafeImage(Long memberId, String imgUrl) {
        this.memberId = memberId;
        this.imgUrl = imgUrl;
    }
}
