package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.domain.*;
import mocacong.server.dto.response.CafeImageReportResponse;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.exception.badrequest.DuplicateReportCafeImageException;
import mocacong.server.exception.badrequest.DuplicateReportCommentException;
import mocacong.server.exception.badrequest.InvalidCafeImageReportException;
import mocacong.server.exception.badrequest.InvalidCommentReportException;
import mocacong.server.exception.notfound.NotFoundCafeImageException;
import mocacong.server.exception.notfound.NotFoundCommentException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.CafeImageRepository;
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
    private final CafeImageRepository cafeImageRepository;

    public CommentReportResponse reportComment(Long memberId, Long commentId, String reportReason) {
        Member reporter = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NotFoundCommentException::new);

        try {
            createCommentReport(comment, reporter, reportReason);

            // 코멘트를 작성한 회원이 탈퇴한 경우
            if (comment.isDeletedMember()) {
                if (comment.isReportThresholdExceeded()) {
                    maskReportedComment(comment);
                }
            } else {
                Member commenter = comment.getMember();
                validateCommentReporter(reporter, comment);
                validateCommentReportThreshold(comment);
                commenter.incrementMemberReportCount();
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReportCommentException();
        }
        return new CommentReportResponse(comment.getReportsCount());
    }

    private void createCommentReport(Comment comment, Member reporter, String reportReason) {
        if (comment.hasAlreadyReported(reporter)) {
            throw new DuplicateReportCommentException();
        }
        ReportReason reason = ReportReason.from(reportReason);
        comment.addReport(new Report(comment, reporter, reason));
    }

    private void maskReportedComment(Comment comment) {
        comment.maskComment();
        comment.maskAuthor();
        comment.updateIsMasked(true);
    }

    private void validateCommentReporter(Member reporter, Comment comment) {
        if (comment.isWrittenByMember(reporter)) {
            throw new InvalidCommentReportException();
        }
    }

    private void validateCommentReportThreshold(Comment comment) {
        if (comment.isReportThresholdExceeded()) {
            maskReportedComment(comment);
        }
    }

    @EventListener
    public void updateCommentReportWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        reportRepository.findAllByReporter(member)
                .forEach(Report::removeReporter);
    }

    public CafeImageReportResponse reportCafeImage(Long memberId, Long cafeImageId, String reportReason) {
        Member reporter = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        CafeImage cafeImage = cafeImageRepository.findById(cafeImageId)
                .orElseThrow(NotFoundCafeImageException::new);
        try {
            createCafeImageReport(cafeImage, reporter, reportReason);

            // 카페 이미지를 등록한 회원이 탈퇴한 경우
            if (cafeImage.isDeletedMember()) {
                if (cafeImage.isReportThresholdExceeded()) {
                    cafeImage.maskCafeImage();
                }
            } else {
                Member author = cafeImage.getMember();
                validateCafeImageReporter(reporter, cafeImage);
                validateCafeImageReportThreshold(cafeImage);
                author.incrementMemberReportCount();
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReportCafeImageException();
        }
        return new CafeImageReportResponse(cafeImage.getReportsCount(), reporter.getReportCount());
    }

    private void createCafeImageReport(CafeImage cafeImage, Member reporter, String reportReason) {
        if (cafeImage.hasAlreadyReported(reporter)) {
            throw new DuplicateReportCafeImageException();
        }
        ReportReason reason = ReportReason.from(reportReason);
        cafeImage.addReport(new Report(cafeImage, reporter, reason));
    }

    private void validateCafeImageReporter(Member reporter, CafeImage cafeImage) {
        if (cafeImage.isSavedByMember(reporter)) {
            throw new InvalidCafeImageReportException();
        }
    }

    private void validateCafeImageReportThreshold(CafeImage cafeImage) {
        if (cafeImage.isReportThresholdExceeded()) {
            cafeImage.maskCafeImage();
        }
    }

    @EventListener
    public void updateCafeImageReportWhenMemberDelete(DeleteMemberEvent event) {
        Member member = event.getMember();
        reportRepository.findAllByReporter(member)
                .forEach(Report::removeReporter);
    }
}
