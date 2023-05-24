package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CafeFilterRequest;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.request.CafeReviewUpdateRequest;
import mocacong.server.dto.response.*;
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

    @Operation(summary = "특정 카페 미리보기 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{mapId}/preview")
    public ResponseEntity<PreviewCafeResponse> previewCafeByMapId(
            @LoginUserEmail String email,
            @PathVariable String mapId
    ) {
        PreviewCafeResponse response = cafeService.previewCafeByMapId(email, mapId);
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

    @Operation(summary = "특정 카페 내가 작성한 리뷰 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{mapId}/me")
    public ResponseEntity<CafeMyReviewResponse> findMyCafeReview(
            @LoginUserEmail String email,
            @PathVariable String mapId
    ) {
        CafeMyReviewResponse response = cafeService.findMyCafeReview(email, mapId);
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
    @PostMapping(value = "/studytype")
    public ResponseEntity<CafeFilterResponse> getCafesByStudyType(@RequestParam(required = false) String studytype,
                                                                  @RequestBody CafeFilterRequest request) {
        CafeFilterResponse responseBody = cafeService.filterCafesByStudyType(studytype, request);
        return ResponseEntity.ok(responseBody);
    }

    @Operation(summary = "카페 이미지 업로드")
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = "/{mapId}/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveCafeImage(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestParam(value = "file") MultipartFile multipartFile
    ) {
        cafeService.saveCafeImage(email, mapId, multipartFile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카페 이미지 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{mapId}/img")
    public ResponseEntity<CafeImagesResponse> getCafeImages(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        CafeImagesResponse response = cafeService.findCafeImages(email, mapId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 이미지 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/{mapId}/img/{cafeImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateCafeImage(
            @LoginUserEmail String email,
            @PathVariable String mapId,
            @PathVariable Long cafeImageId,
            @RequestParam(value = "file") MultipartFile multipartFile
    ) {
        cafeService.updateCafeImage(email, mapId, cafeImageId, multipartFile);
        return ResponseEntity.ok().build();
    }
}
