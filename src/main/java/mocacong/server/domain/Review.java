package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

import javax.persistence.*;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", unique = true)
    private Cafe cafe;

    @Embedded
    private CafeDetail cafeDetail;

    public Review(Member member, Cafe cafe, CafeDetail cafeDetail) {
        this.member = member;
        this.cafe = cafe;
        this.cafe.addReview(this);
        this.cafeDetail = cafeDetail;
    }

    public StudyType getStudyType() {
        return cafeDetail.getStudyType();
    }

    public Wifi getWifi() {
        return cafeDetail.getWifi();
    }

    public Parking getParking() {
        return cafeDetail.getParking();
    }

    public Toilet getToilet() {
        return cafeDetail.getToilet();
    }

    public Desk getDesk() {
        return cafeDetail.getDesk();
    }

    public Power getPower() {
        return cafeDetail.getPower();
    }

    public Sound getSound() {
        return cafeDetail.getSound();
    }

    public void updateReview(CafeDetail newCafeDetail) {
        this.cafeDetail = newCafeDetail;
    }

    public void removeMember() {
        this.member = null;
    }
}
