package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "cafe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "map_id", nullable = false)
    private String mapId;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> score;

    @Embedded
    private CafeDetail cafeDetail;

    @Embedded
    private CafeImage cafeImage;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public Cafe(String mapId, String name) {
        this.mapId = mapId;
        this.name = name;
        this.cafeDetail = new CafeDetail();
        this.cafeImage = new CafeImage();
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void updateCafeDetails() {
        StudyType studyType = getMostFrequentStudyType();
        Wifi wifi = (Wifi) getMostFrequentType(reviews.stream().map(Review::getWifi));
        Parking parking = (Parking) getMostFrequentType(reviews.stream().map(Review::getParking));
        Toilet toilet = (Toilet) getMostFrequentType(reviews.stream().map(Review::getToilet));
        Desk desk = (Desk) getMostFrequentType(reviews.stream().map(Review::getDesk));
        Power power = (Power) getMostFrequentType(reviews.stream().map(Review::getPower));
        Sound sound = (Sound) getMostFrequentType(reviews.stream().map(Review::getSound));

        this.cafeDetail = new CafeDetail(studyType, wifi, parking, toilet, desk, power, sound);
    }

    private StudyType getMostFrequentStudyType() {
        int solo = 0, group = 0;
        for (Review review : reviews) {
            StudyType studyType = review.getStudyType();
            if (studyType == StudyType.SOLO) solo++;
            else if (studyType == StudyType.GROUP) group++;
        }

        if (solo == 0 && group == 0) return null;
        else if (solo == group) return StudyType.BOTH;
        else if (solo > group) return StudyType.SOLO;
        else return StudyType.GROUP;
    }

    private Object getMostFrequentType(Stream<Object> stream) {
        Map.Entry<Object, Long> frequentTypeInfo = stream
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        return frequentTypeInfo != null ? frequentTypeInfo.getKey() : null;
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

    public void updateCafeImgUrl(String imgUrl) {
        if (this.cafeImage == null) {
            this.cafeImage = new CafeImage();
        }
        this.cafeImage = new CafeImage(this.cafeImage.getMemberId(), imgUrl);
    }
}
