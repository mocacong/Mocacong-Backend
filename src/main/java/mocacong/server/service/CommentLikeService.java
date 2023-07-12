package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Comment;
import mocacong.server.domain.CommentLike;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.CommentLikeSaveResponse;
import mocacong.server.exception.badrequest.AlreadyExistsCommentLike;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundCommentLikeException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CommentLikeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteMemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        try {
            CommentLike commentLike = new CommentLike(member, comment);
            return new CommentLikeSaveResponse(commentLikeRepository.save(commentLike).getId());
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsCommentLike();
        }
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
}
