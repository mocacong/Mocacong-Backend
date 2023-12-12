package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.CafeDetail;
import mocacong.server.domain.cafedetail.StudyType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MyReviewCafeResponse {

    private String mapId;
    private String name;
    private String myStudyType;
    private int myScore;
    private String roadAddress;
    private String myWifi;
    private String myParking;
    private String myToilet;
    private String myPower;
    private String mySound;
    private String myDesk;

    public MyReviewCafeResponse(String mapId, String name, StudyType myStudyType, int myScore, String roadAddress, CafeDetail cafeDetail) {
        this.mapId = mapId;
        this.name = name;
        this.myStudyType = myStudyType.getValue();
        this.myScore = myScore;
        this.roadAddress = roadAddress;
        this.myWifi = cafeDetail.getWifiValue();
        this.myParking = cafeDetail.getParkingValue();
        this.myToilet = cafeDetail.getToiletValue();
        this.myPower = cafeDetail.getPowerValue();
        this.mySound = cafeDetail.getSoundValue();
        this.myDesk = cafeDetail.getDeskValue();
    }
}
