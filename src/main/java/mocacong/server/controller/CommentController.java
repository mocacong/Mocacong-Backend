package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CommentSaveRequest;
import mocacong.server.dto.response.CommentSaveResponse;
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
    public ResponseEntity<CommentSaveResponse> saveCafeReview(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestBody CommentSaveRequest request
    ) {
        CommentSaveResponse response = commentService.save(email, mapId, request.getContent());
        return ResponseEntity.ok(response);
    }
}
