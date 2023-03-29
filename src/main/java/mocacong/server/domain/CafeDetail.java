package mocacong.server.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeDetail {

    @Column(name = "study_type")
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    @Column(name = "wifi")
    @Enumerated(EnumType.STRING)
    private Wifi wifi;

    @Column(name = "parking")
    @Enumerated(EnumType.STRING)
    private Parking parking;

    @Column(name = "toilet")
    @Enumerated(EnumType.STRING)
    private Toilet toilet;

    @Column(name = "desk")
    @Enumerated(EnumType.STRING)
    private Desk desk;

    @Column(name = "power")
    @Enumerated(EnumType.STRING)
    private Power power;

    @Column(name = "sound")
    @Enumerated(EnumType.STRING)
    private Sound sound;
}
