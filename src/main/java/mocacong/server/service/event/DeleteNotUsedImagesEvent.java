package mocacong.server.service.event;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeleteNotUsedImagesEvent {

    private final List<String> imgUrls;
}
