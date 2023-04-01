package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.response.CafeReviewResponse;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.security.auth.LoginUserEmail;
import mocacong.server.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Cafes", description = "카페")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cafes")
public class CafeController {

    private final CafeService cafeService;

    @Operation(summary = "카페등록")
    @PostMapping
    public ResponseEntity<Void> cafeRegister(@RequestBody @Valid CafeRegisterRequest request) {
        cafeService.save(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 카페 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{mapId}")
    public ResponseEntity<FindCafeResponse> findCafeByMapId(
            @LoginUserEmail String email,
            @PathVariable String mapId
    ) {
        FindCafeResponse response = cafeService.findCafeByMapId(email, mapId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 카페 리뷰 작성")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{mapId}")
    public ResponseEntity<CafeReviewResponse> saveCafeReview(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestBody CafeReviewRequest request
    ) {
        CafeReviewResponse response = cafeService.saveCafeReview(email, mapId, request);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "특정 카페 리뷰 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{mapId}")
    public ResponseEntity<CafeReviewResponse> updateCafeReview(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestBody CafeReviewRequest request
    ) {
        CafeReviewResponse response = cafeService.updateCafeReview(email, mapId, request);
        return ResponseEntity.ok(response);
    }
}
