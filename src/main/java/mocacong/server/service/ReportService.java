package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.domain.Comment;
import mocacong.server.domain.Member;
import mocacong.server.domain.Report;
import mocacong.server.domain.ReportReason;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.exception.badrequest.DuplicateReportCommentException;
import mocacong.server.exception.badrequest.InvalidCommentReportException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.ReportRepository;
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
    private final ReportRepository reportRepository;

    public CommentReportResponse reportComment(Long memberId, Long commentId, String reportReason) {
        Member reporter = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NotFoundCommentException::new);

        try {
            createCommentReport(comment, reporter, reportReason);

            // 코멘트를 작성한 회원이 탈퇴한 경우
            if (comment.isDeletedCommenter() && comment.isReportThresholdExceeded()) {
                maskReportedComment(comment);
                comment.updateIsMasked(true);
            } else {
                Member commenter = comment.getMember();
                if (comment.isWrittenByMember(reporter)) {
                    throw new InvalidCommentReportException();
                }
                if (comment.isReportThresholdExceeded()) {
                    commenter.incrementMemberReportCount();
                    maskReportedComment(comment);
                    comment.updateIsMasked(true);
                }
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReportCommentException();
        }
        return new CommentReportResponse(comment.getReportsCount(), reporter.getReportCount());
    }

    private void createCommentReport(Comment comment, Member reporter, String reportReason) {
        if (comment.hasAlreadyReported(reporter)) {
            throw new DuplicateReportCommentException();
        }
        ReportReason reason = ReportReason.from(reportReason);
        comment.addReport(new Report(comment, reporter, reason));
    }

    @EventListener
    public void updateCommentReportWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        reportRepository.findAllByReporter(member)
                .forEach(Report::removeReporter);
    }

    private void maskReportedComment(Comment comment) {
        comment.maskComment();
        comment.maskAuthor();
    }
}
