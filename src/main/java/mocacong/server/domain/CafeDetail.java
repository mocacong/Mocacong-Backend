package mocacong.server.domain;

import lombok.*;
import mocacong.server.domain.cafedetail.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeDetail {

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

    public void setWifi(Wifi wifi) {
        this.wifi = wifi;
    }

    public void setParking(Parking parking) {
        this.parking = parking;
    }

    public void setToilet(Toilet toilet) {
        this.toilet = toilet;
    }

    public void setDesk(Desk desk) {
        this.desk = desk;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }
}
