package mocacong.server.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.cafedetail.*;

@Embeddable
@NoArgsConstructor
@Getter
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

    public CafeDetail(StudyType studyType, Wifi wifi, Parking parking, Toilet toilet, Desk desk, Power power, Sound sound) {
        this.studyType = studyType;
        this.wifi = wifi;
        this.parking = parking;
        this.toilet = toilet;
        this.desk = desk;
        this.power = power;
        this.sound = sound;
    }

    public String getStudyTypeValue() {
        return studyType != null ? studyType.getValue() : null;
    }

    public String getWifiValue() {
        return wifi != null ? wifi.getValue() : null;
    }

    public String getParkingValue() {
        return parking != null ? parking.getValue() : null;
    }

    public String getToiletValue() {
        return toilet != null ? toilet.getValue() : null;
    }

    public String getDeskValue() {
        return desk != null ? desk.getValue() : null;
    }

    public String getPowerValue() {
        return power != null ? power.getValue() : null;
    }

    public String getSoundValue() {
        return sound != null ? sound.getValue() : null;
    }
}
