package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.response.CommentLikeSaveResponse;
import mocacong.server.security.auth.LoginUserId;
import mocacong.server.service.CommentLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CommentsLike", description = "댓글 좋아요")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments/{commentId}/like")
public class CommentLikeController {
    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글 좋아요 등록")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<CommentLikeSaveResponse> saveCommentLike(
            @LoginUserId Long memberId,
            @PathVariable Long commentId
    ) {
        CommentLikeSaveResponse response = commentLikeService.save(memberId, commentId);
        return ResponseEntity.ok(response);
    }
}
