package mocacong.server.domain;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_profile_image_id")
    private Long id;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "is_used")
    private Boolean isUsed;

    public MemberProfileImage(String imgUrl, Boolean isUsed) {
        this.imgUrl = imgUrl;
        this.isUsed = isUsed;
    }

    public MemberProfileImage(String imgUrl) {
        this(imgUrl, true);
    }

    public void updateNotUsedStatus() {
        this.isUsed = false;
    }
}
