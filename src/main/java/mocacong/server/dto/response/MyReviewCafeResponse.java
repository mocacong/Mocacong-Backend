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
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;

    public MyReviewCafeResponse(String mapId, String name, StudyType myStudyType, int myScore, String roadAddress, CafeDetail cafeDetail) {
        this.mapId = mapId;
        this.name = name;
        this.myStudyType = myStudyType.getValue();
        this.myScore = myScore;
        this.roadAddress = roadAddress;
        this.wifi = cafeDetail.getWifiValue();
        this.parking = cafeDetail.getParkingValue();
        this.toilet = cafeDetail.getToiletValue();
        this.power = cafeDetail.getPowerValue();
        this.sound = cafeDetail.getSoundValue();
        this.desk = cafeDetail.getDeskValue();
    }
}
