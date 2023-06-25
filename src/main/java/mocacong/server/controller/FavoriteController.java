package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.response.FavoriteSaveResponse;
import mocacong.server.security.auth.LoginUserId;
import mocacong.server.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Favorites", description = "즐겨찾기")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cafes/{mapId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "카페 즐겨찾기 등록")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<FavoriteSaveResponse> saveFavoriteCafe(
            @LoginUserId Long memberId,
            @PathVariable String mapId
    ) {
        FavoriteSaveResponse response = favoriteService.save(memberId, mapId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카페 즐겨찾기 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ResponseEntity<Void> deleteFavoriteCafe(
            @LoginUserId Long memberId,
            @PathVariable String mapId
    ) {
        favoriteService.delete(memberId, mapId);
        return ResponseEntity.ok().build();
    }
}
