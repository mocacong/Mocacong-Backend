package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class PreviewCafeResponse {

    private String name;
    private String roadAddress;
    private Boolean favorite;
    private double score;
    private String studyType;
    private int reviewsCount;
}
