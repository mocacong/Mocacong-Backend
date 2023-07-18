package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CommentReportRequest;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.security.auth.LoginUserId;
import mocacong.server.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Reports", description = "신고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "카페 코멘트 신고")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<CommentReportResponse> reportComment(
            @LoginUserId Long memberId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentReportRequest request
    ) {
        CommentReportResponse response = reportService.reportComment(memberId, commentId, request.getMyReportReason());
        return ResponseEntity.ok(response);
    }
}
