package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.CommentUpdateRequest;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.security.auth.LoginUserId;
import mocacong.server.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Comments", description = "댓글")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cafes/{mapId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "카페 코멘트 작성")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<CommentSaveResponse> saveComment(
            @LoginUserId Long memberId,
            @PathVariable String mapId,
            @RequestBody @Valid CommentSaveRequest request
    ) {
        CommentSaveResponse response = commentService.save(memberId, mapId, request.getContent());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<CommentsResponse> findComments(
            @LoginUserId Long memberId,
            @PathVariable String mapId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        CommentsResponse response = commentService.findAll(memberId, mapId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 중 나의 코멘트만 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    public ResponseEntity<CommentsResponse> findCommentsByMe(
            @LoginUserId Long memberId,
            @PathVariable String mapId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        CommentsResponse response = commentService.findCafeCommentsOnlyMyComments(memberId, mapId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @LoginUserId Long memberId,
            @PathVariable String mapId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        commentService.update(memberId, mapId, request.getContent(), commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카페 코멘트 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @LoginUserId Long memberId,
            @PathVariable String mapId,
            @PathVariable Long commentId
    ) {
        commentService.delete(memberId, mapId, commentId);
        return ResponseEntity.ok().build();
    }
}
