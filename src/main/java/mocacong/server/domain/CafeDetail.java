package mocacong.server.domain;

import lombok.*;
import mocacong.server.domain.cafedetail.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@NoArgsConstructor
@Getter
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

    public CafeDetail(Wifi wifi, Parking parking, Toilet toilet, Desk desk, Power power, Sound sound) {
        this.wifi = wifi;
        this.parking = parking;
        this.toilet = toilet;
        this.desk = desk;
        this.power = power;
        this.sound = sound;
    }
}
