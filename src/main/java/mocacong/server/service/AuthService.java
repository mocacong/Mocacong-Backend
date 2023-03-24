package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.IdPasswordMismatchException;
import mocacong.server.exception.notfound.NoSuchMemberException;
import mocacong.server.infrastructure.auth.JwtUtils;
import mocacong.server.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(final AuthLoginRequest authLoginRequest) {
        Member findMember = memberRepository.findByEmail(authLoginRequest.getEmail())
                .orElseThrow(NoSuchMemberException::new);

        validatePassword(findMember, authLoginRequest.getPassword());

        String token = issueToken(findMember);

        return TokenResponse.from(token);
    }

    private String issueToken(final Member findMember) {
        Map<String, Object> payload = JwtUtils.payloadBuilder()
                .setSubject(findMember.getEmail())
                .build();

        return jwtUtils.createToken(payload);
    }

    private void validatePassword(final Member findMember, final String password) {
        if (!passwordEncoder.matches(password, findMember.getPassword())) {
            throw new IdPasswordMismatchException();
        }
    }
}
