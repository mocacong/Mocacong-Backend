package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.request.CommentUpdateRequest;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.security.auth.LoginUserEmail;
import mocacong.server.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestBody @Valid CommentSaveRequest request
    ) {
        CommentSaveResponse response = commentService.save(email, mapId, request.getContent());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<CommentsResponse> findComments(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        CommentsResponse response = commentService.findAll(email, mapId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 중 나의 코멘트만 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    public ResponseEntity<CommentsResponse> findCommentsByMe(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        CommentsResponse response = commentService.findCafeCommentsOnlyMyComments(email, mapId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 코멘트 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        commentService.update(email, mapId, request.getContent(), commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카페 코멘트 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @PathVariable Long commentId
    ) {
        commentService.delete(email, mapId, commentId);
        return ResponseEntity.ok().build();
    }
}
