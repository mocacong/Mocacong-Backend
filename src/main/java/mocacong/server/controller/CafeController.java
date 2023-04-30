package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CafeFilterRequest;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.request.CafeReviewUpdateRequest;
import mocacong.server.dto.response.CafeFilterResponse;
import mocacong.server.dto.response.CafeReviewResponse;
import mocacong.server.dto.response.CafeReviewUpdateResponse;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.security.auth.LoginUserEmail;
import mocacong.server.service.CafeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<CafeReviewUpdateResponse> updateCafeReview(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestBody CafeReviewUpdateRequest request
    ) {
        CafeReviewUpdateResponse response = cafeService.updateCafeReview(email, mapId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "StudyType별로 카페 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<CafeFilterResponse> getCafesByStudyType(@RequestParam(required = false) String studytype,
                                                                  @RequestBody CafeFilterRequest requestBody) {
        CafeFilterResponse responseBody = cafeService.filterCafesByStudyType(studytype, requestBody);
        return ResponseEntity.ok(responseBody);
    }

    @Operation(summary = "카페 이미지 업로드")
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = "/{mapId}/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateCafeImage(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestParam(value = "file") MultipartFile multipartFile
    ) {
        cafeService.saveCafeImage(email, mapId, multipartFile);
        return ResponseEntity.ok().build();
    }
}
