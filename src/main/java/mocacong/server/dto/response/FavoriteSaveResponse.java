package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class FavoriteSaveResponse {

    private Long favoriteId;

    private int userReportCount;
}
