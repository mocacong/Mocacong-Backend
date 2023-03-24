package mocacong.server.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Embedded
    private Coordinate coordinate;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> score;

    @Embedded
    private CafeDetail cafeDetail;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public Cafe(String name, BigDecimal x, BigDecimal y) {
        this.name = name;
        this.coordinate = new Coordinate(x, y);
        this.cafeDetail = null;
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void updateCafeDetails() {
        StudyType studyType = (StudyType) getMostFrequentType(reviews.stream().map(Review::getStudyType));
        Wifi wifi = (Wifi) getMostFrequentType(reviews.stream().map(Review::getWifi));
        Parking parking = (Parking) getMostFrequentType(reviews.stream().map(Review::getParking));
        Toilet toilet = (Toilet) getMostFrequentType(reviews.stream().map(Review::getToilet));
        Desk desk = (Desk) getMostFrequentType(reviews.stream().map(Review::getDesk));
        Power power = (Power) getMostFrequentType(reviews.stream().map(Review::getPower));
        Sound sound = (Sound) getMostFrequentType(reviews.stream().map(Review::getSound));
        Tumbler tumbler = (Tumbler) getMostFrequentType(reviews.stream().map(Review::getTumbler));

        this.cafeDetail = new CafeDetail(studyType, wifi, parking, toilet, desk, power, sound, tumbler);
    }

    private Object getMostFrequentType(Stream<Object> stream) {
        return stream.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalArgumentException("카페 세부정보 갱신 중 예외가 발생했습니다."))
                .getKey();
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
}
