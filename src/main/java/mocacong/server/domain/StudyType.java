package mocacong.server.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidStudyTypeException;

import javax.persistence.*;

@Entity
@Table(name = "study_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyType {

    private static final String SOLO_STUDY_TYPE = "solo";
    private static final String GROUP_STUDY_TYPE = "group";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_type_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    @Column(name = "study_type_value", nullable = false)
    private String studyTypeValue;

    public StudyType(Member member, Cafe cafe, String studyTypeValue) {
        this.member = member;
        this.cafe = cafe;
        this.cafe.addStudyType(this);

        String lowerCaseStudyTypeValue = studyTypeValue.toLowerCase();
        validateValue(lowerCaseStudyTypeValue);
        this.studyTypeValue = lowerCaseStudyTypeValue;
    }

    private void validateValue(String studyTypeValue) {
        if (!studyTypeValue.equals(SOLO_STUDY_TYPE) && !studyTypeValue.equals(GROUP_STUDY_TYPE)) {
            throw new InvalidStudyTypeException();
        }
    }

    public void updateStudyTypeValue(String studyTypeValue) {
        String lowerCaseStudyTypeValue = studyTypeValue.toLowerCase();
        validateValue(lowerCaseStudyTypeValue);
        this.studyTypeValue = lowerCaseStudyTypeValue;
    }
}
