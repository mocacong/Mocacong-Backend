package mocacong.server.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> score;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "cafe", fetch = FetchType.LAZY)
    private List<Comment> comments;

    public Cafe(String name) {
        this.name = name;
        this.score = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void addScore(Score score) {
        this.score.add(score);
    }

    // TODO: Cafe Detail 점수를 얻기 위한 좋은 설계 고민하기

    public double findAverageScore() {
        return score.stream()
                .mapToDouble(Score::getScore)
                .average()
                .orElse(0.0);
    }
}
