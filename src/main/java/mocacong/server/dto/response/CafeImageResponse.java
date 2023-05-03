package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeImageResponse {
    private int currentPage;
    private List<CafeImageResponse> cafeImages;
    private boolean isMe;

    public static CafeImageResponse of(int currentPage, List<CafeImageResponse> cafeImages, boolean isMe) {
        return new CafeImageResponse(currentPage, cafeImages, isMe);
    }

    public static CafeImageResponse of(int currentPage, List<CafeImageResponse> cafeImages) {
        return new CafeImageResponse(currentPage, cafeImages, false);
    }
}
