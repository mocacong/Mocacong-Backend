package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.exception.badrequest.InvalidCommentDeleteException;
import mocacong.server.exception.badrequest.InvalidCommentUpdateException;
import mocacong.server.exception.notfound.NotFoundCafeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteCommentEvent;
import mocacong.server.service.event.DeleteMemberEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CommentSaveResponse save(Long memberId, String mapId, String content) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = new Comment(cafe, member, content);

        return new CommentSaveResponse(commentRepository.save(comment).getId());
    }

    @Transactional(readOnly = true)
    public CommentsResponse findAll(Long memberId, String mapId, Integer page, int count) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Slice<Comment> comments = commentRepository.findAllByCafeId(cafe.getId(), PageRequest.of(page, count));
        List<CommentResponse> responses = findCommentResponses(member, comments);

        if (page == 0) {
            Long totalCounts = commentRepository.countAllByCafeId(cafe.getId());
            return new CommentsResponse(comments.isLast(), totalCounts, responses);
        }
        return new CommentsResponse(comments.isLast(), responses);
    }

    @Transactional(readOnly = true)
    public CommentsResponse findCafeCommentsOnlyMyComments(Long memberId, String mapId, Integer page, int count) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Slice<Comment> comments =
                commentRepository.findAllByCafeIdAndMemberId(cafe.getId(), member.getId(), PageRequest.of(page, count));
        List<CommentResponse> responses = findCommentResponses(member, comments);
        return new CommentsResponse(comments.isLast(), responses);
    }

    private List<CommentResponse> findCommentResponses(Member member, Slice<Comment> comments) {
        return comments.stream()
                .map(comment -> {
                    if (comment.isWrittenByMember(member)) {
                        return new CommentResponse(comment.getId(), member.getImgUrl(), member.getNickname(), comment.getContent(), true);
                    } else {
                        return new CommentResponse(comment.getId(), comment.getWriterImgUrl(), comment.getWriterNickname(), comment.getContent(), false);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long memberId, String mapId, String content, Long commentId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
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

    @Transactional
    public void delete(Long memberId, String mapId, Long commentId) {
        Cafe cafe = cafeRepository.findByMapId(mapId)
                .orElseThrow(NotFoundCafeException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = cafe.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(NotFoundCommentException::new);

        if (!comment.isWrittenByMember(member)) {
            throw new InvalidCommentDeleteException();
        }
        applicationEventPublisher.publishEvent(new DeleteCommentEvent(comment));
        commentRepository.delete(comment);
    }

    @EventListener
    public void updateCommentWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        commentRepository.findAllByMemberId(member.getId())
                .forEach(Comment::removeMember);
    }
}
