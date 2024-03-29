package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyFavoriteCafeResponse {
    private String mapId;
    private String name;
    private String studyType;
    private double score;
    private String roadAddress;
}
