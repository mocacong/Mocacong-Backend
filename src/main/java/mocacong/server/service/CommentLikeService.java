package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Comment;
import mocacong.server.domain.CommentLike;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentLikeSaveResponse;
import mocacong.server.exception.badrequest.AlreadyExistsCommentLike;
import mocacong.server.exception.badrequest.InvalidCommentLikeException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundCommentLikeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CommentLikeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteCommentEvent;
import mocacong.server.service.event.DeleteMemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentLikeSaveResponse save(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NotFoundCommentException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        validateDuplicateCommentLike(memberId, commentId);
        validateIsNotOwnComment(comment, member);

        try {
            CommentLike commentLike = new CommentLike(member, comment);
            return new CommentLikeSaveResponse(commentLikeRepository.save(commentLike).getId());
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsCommentLike();
        }
    }

    private void validateIsNotOwnComment(Comment comment, Member member) {
        if (comment.isWrittenByMember(member)) {
            throw new InvalidCommentLikeException();
        }
    }

    private void validateDuplicateCommentLike(Long memberId, Long commentId) {
        commentLikeRepository.findCommentLikeIdByCommentIdAndMemberId(memberId, commentId).ifPresent(cl -> {
            throw new AlreadyExistsCommentLike();
        });
    }

    @Transactional
    public void delete(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NotFoundCommentException::new);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        Long commentLikeId = commentLikeRepository
                .findCommentLikeIdByCommentIdAndMemberId(member.getId(), comment.getId())
                .orElseThrow(NotFoundCommentLikeException::new);

        commentLikeRepository.deleteById(commentLikeId);
    }

    @EventListener
    public void deleteAllWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        commentLikeRepository.findAllByMemberId(member.getId()).forEach(CommentLike::removeMember);
    }

    @EventListener
    public void deleteAllWhenCommentDelete(DeleteCommentEvent event) {
        Comment comment = event.getComment();
        commentLikeRepository.findAllByCommentId(comment.getId()).forEach(CommentLike::removeComment);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void deleteFavoritesWhenMemberDeleted(DeleteCommentEvent event) {
        commentLikeRepository.deleteAllByCommentIdIsNull();
    }
}
