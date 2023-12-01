package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cafe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe extends BaseTime {

    private static final int NONE_REVIEW_SCORE = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "map_id", unique = true, nullable = false)
    private String mapId;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> score;

    @Embedded
    private CafeDetail cafeDetail;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<CafeImage> cafeImages;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public Cafe(String mapId, String name) {
        this.mapId = mapId;
        this.name = name;
        this.cafeDetail = new CafeDetail();
        this.cafeImages = new ArrayList<>();
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public Cafe(String mapId, String name, String roadAddress) {
        this.mapId = mapId;
        this.name = name;
        this.roadAddress = roadAddress;
        this.cafeDetail = new CafeDetail();
        this.cafeImages = new ArrayList<>();
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public Cafe(String mapId, String name, String roadAddress, String phoneNumber) {
        this.mapId = mapId;
        this.name = name;
        this.roadAddress = roadAddress;
        this.phoneNumber = phoneNumber;
        this.cafeDetail = new CafeDetail();
        this.cafeImages = new ArrayList<>();
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void updateCafeDetails() {
        StudyType studyType = getMostFrequentStudyType();
        Wifi wifi = Wifi.averageFrom(reviews.stream().map(Review::getWifi)
                .filter(Objects::nonNull).mapToDouble(Wifi::getScore).average().orElse(NONE_REVIEW_SCORE));
        Parking parking = Parking.averageFrom(reviews.stream().map(Review::getParking)
                .filter(Objects::nonNull).mapToDouble(Parking::getScore).average().orElse(NONE_REVIEW_SCORE));
        Toilet toilet = Toilet.averageFrom(reviews.stream().map(Review::getToilet)
                .filter(Objects::nonNull).mapToDouble(Toilet::getScore).average().orElse(NONE_REVIEW_SCORE));
        Desk desk = Desk.averageFrom(reviews.stream().map(Review::getDesk)
                .filter(Objects::nonNull).mapToDouble(Desk::getScore).average().orElse(NONE_REVIEW_SCORE));
        Power power = Power.averageFrom(reviews.stream().map(Review::getPower)
                .filter(Objects::nonNull).mapToDouble(Power::getScore).average().orElse(NONE_REVIEW_SCORE));
        Sound sound = Sound.averageFrom(reviews.stream().map(Review::getSound)
                .filter(Objects::nonNull).mapToDouble(Sound::getScore).average().orElse(NONE_REVIEW_SCORE));
        this.cafeDetail = new CafeDetail(studyType, wifi, parking, toilet, desk, power, sound);
    }

    public void updateCafeRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    private StudyType getMostFrequentStudyType() {
        int solo = 0, group = 0;
        for (Review review : reviews) {
            StudyType studyType = review.getStudyType();
            if (studyType == StudyType.SOLO) solo++;
            else if (studyType == StudyType.GROUP) group++;
            else if (studyType == StudyType.BOTH) {
                solo++;
                group++;
            }
        }

        if (solo == 0 && group == 0) return null;
        else if (solo == group) return StudyType.BOTH;
        else if (solo > group) return StudyType.SOLO;
        else return StudyType.GROUP;
    }

    public String getStudyType() {
        return cafeDetail.getStudyTypeValue();
    }

    public double findAverageScore() {
        return score.stream()
                .mapToDouble(Score::getScore)
                .average()
                .orElse(0.0);
    }

    public void addScore(Score score) {
        this.score.add(score);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cafe)) {
            return false;
        }
        Cafe cafe = (Cafe) o;
        return Objects.equals(mapId, cafe.mapId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapId);
    }
}
