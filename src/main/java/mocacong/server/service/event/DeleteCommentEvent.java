package mocacong.server.service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Comment;

@Getter
@RequiredArgsConstructor
public class DeleteCommentEvent {

    private final Comment comment;
}
