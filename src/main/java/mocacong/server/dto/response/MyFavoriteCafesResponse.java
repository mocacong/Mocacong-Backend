package mocacong.server.dto.response;

import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyFavoriteCafesResponse {

    private int currentPage;
    private List<MyFavoriteCafeResponse> cafes;
}
