package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.exception.badrequest.InvalidCommentUpdateException;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.MemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;
    private final CommentRepository commentRepository;

    public CommentSaveResponse save(String email, String mapId, String content) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = new Comment(cafe, member, content);

        return new CommentSaveResponse(commentRepository.save(comment).getId());
    }

    @Transactional
    public void update(String email, String mapId, String content, Long commentId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = cafe.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(NotFoundCommentException::new);

        if (!comment.isWrittenByMember(member)) {
            throw new InvalidCommentUpdateException();
        }
        comment.updateComment(content);
    }

    @EventListener
    public void updateCommentWhenMemberDelete(MemberEvent event) {
        Member member = event.getMember();
        commentRepository.findAllByMemberId(member.getId())
                .forEach(Comment::removeMember);
    }
}
