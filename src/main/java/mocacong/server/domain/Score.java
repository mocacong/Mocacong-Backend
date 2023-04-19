package mocacong.server.domain;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidScoreException;

@Entity
@Table(name = "score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score extends BaseTime {

    private static final int MAXIMUM_SCORE = 5;
    private static final int MINIMUM_SCORE = 1;

    @Id
    @Column(name = "score_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score", nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    public Score(int score, Member member, Cafe cafe) {
        validateScoreRange(score);
        this.score = score;
        this.member = member;
        this.cafe = cafe;
        this.cafe.addScore(this);
    }

    private void validateScoreRange(int score) {
        if (score < MINIMUM_SCORE || score > MAXIMUM_SCORE) {
            throw new InvalidScoreException();
        }
    }

    public void updateScore(int score) {
        validateScoreRange(score);
        this.score = score;
    }
}
