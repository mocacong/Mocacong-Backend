package mocacong.server.dto.request;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeFilterFavoritesRequest {
    private List<String> mapIds;
}
