package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.MemberGetResponse;
import mocacong.server.dto.response.MemberSignUpResponse;
import mocacong.server.security.auth.LoginUserEmail;
import mocacong.server.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Members", description = "회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<MemberSignUpResponse> signUp(@RequestBody @Valid MemberSignUpRequest request) {
        MemberSignUpResponse response = memberService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원탈퇴")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ResponseEntity<Void> delete(@LoginUserEmail String email) {
        memberService.delete(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원전체조회")
    @GetMapping
    public ResponseEntity<List<MemberGetResponse>> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        List<MemberGetResponse> response = members.stream()
                .map(member -> new MemberGetResponse(member.getId(), member.getEmail(),
                        member.getNickname(), member.getPhone()))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(response);
    }


}
