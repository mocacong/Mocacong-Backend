package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cafes", description = "카페")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cafes")
public class CafeController {

    private final CafeService cafeService;

    @Operation(summary = "카페등록")
    @PostMapping
    public ResponseEntity<Void> cafeRegister(CafeRegisterRequest request) {
        cafeService.cafeSave(request);
        return ResponseEntity.ok().build();
    }
}
