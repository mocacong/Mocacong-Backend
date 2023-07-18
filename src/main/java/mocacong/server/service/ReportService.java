package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.domain.Report;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.exception.badrequest.DuplicateReportCommentException;
import mocacong.server.exception.badrequest.InvalidCommentReportException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CommentReportRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.service.event.DeleteMemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;

    public CommentReportResponse reportComment(Long memberId, Long commentId, String reportReason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NotFoundCommentException::new);

        try {
            comment.incrementCommentReport(member, reportReason);

            // 코멘트를 작성한 회원이 탈퇴한 경우
            if (comment.getMember() == null && comment.getReportsCount() >= 5) {
                commentRepository.delete(comment);
            } else {
                Member commenter = comment.getMember();
                if (comment.isWrittenByMember(member)) {
                    throw new InvalidCommentReportException();
                }
                if (comment.getReportsCount() >= 5) {
                    commenter.incrementMemberReportCount();
                    commentRepository.delete(comment);
                }
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReportCommentException();
        }
        return new CommentReportResponse(comment.getReportsCount(), member.getReportCount());
    }

    @EventListener
    public void updateCommentReportWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        commentReportRepository.findAllByReporter(member)
                .forEach(Report::removeReporter);
    }
}
