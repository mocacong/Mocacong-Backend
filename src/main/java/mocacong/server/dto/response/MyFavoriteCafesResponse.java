package mocacong.server.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MyFavoriteCafesResponse {

    private int currentPage;
    private List<MyFavoriteCafeResponse> cafes;
}
