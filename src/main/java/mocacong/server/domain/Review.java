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
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_type_id", nullable = false)
    private StudyType studyType;

    @Embedded
    private CafeDetail cafeDetail;

    public Review(Member member, Cafe cafe, StudyType studyType, CafeDetail cafeDetail) {
        this.member = member;
        this.cafe = cafe;
        this.studyType = studyType;
        this.cafe.addReview(this);
        this.cafeDetail = cafeDetail;
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

    public void updateStudyType(String studyType) {
        this.studyType.updateStudyTypeValue(studyType);
    }
}
