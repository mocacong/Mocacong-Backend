package mocacong.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

@Entity
@Table(name = "cafe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe {

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

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyType> studyTypes;

    @Embedded
    private CafeDetail cafeDetail;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public Cafe(String id, String name) {
        this.mapId = id;
        this.name = name;
        this.cafeDetail = new CafeDetail();
        this.score = new ArrayList<>();
        this.studyTypes = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void updateCafeDetails() {
        Wifi wifi = (Wifi) getMostFrequentType(reviews.stream().map(Review::getWifi));
        Parking parking = (Parking) getMostFrequentType(reviews.stream().map(Review::getParking));
        Toilet toilet = (Toilet) getMostFrequentType(reviews.stream().map(Review::getToilet));
        Desk desk = (Desk) getMostFrequentType(reviews.stream().map(Review::getDesk));
        Power power = (Power) getMostFrequentType(reviews.stream().map(Review::getPower));
        Sound sound = (Sound) getMostFrequentType(reviews.stream().map(Review::getSound));

        this.cafeDetail = new CafeDetail(wifi, parking, toilet, desk, power, sound);
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

    public void addStudyType(StudyType studyType) {
        this.studyTypes.add(studyType);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }
}
