package mocacong.server.dto.response;

import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class FindCafeResponse {

    private Boolean favorite;
    private Long favoriteId;
    private String name;
    private String roadAddress;
    private String phoneNumber;
    private double score;
    private Integer myScore;
    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;
    private int reviewsCount;
    private int commentsCount;
    private List<CommentResponse> comments;
    private List<CafeImageResponse> cafeImages;
}
