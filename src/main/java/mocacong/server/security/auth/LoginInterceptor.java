package mocacong.server.security.auth;

import io.jsonwebtoken.ExpiredJwtException;
import mocacong.server.domain.RefreshToken;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.unauthorized.TokenExpiredException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.RefreshTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public LoginInterceptor(final JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository,
                            MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPreflight(request) || isSwaggerRequest(request)) {
            return true;
        }

        String accessToken = AuthorizationExtractor.extractAccessToken(request);
        try {
            // Access Token이 유효한 경우
            jwtTokenProvider.validateToken(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            // Access Token이 만료된 경우
            RefreshToken foundTokenInfo = refreshTokenRepository.findByAccessToken(accessToken)
                    .orElseThrow(TokenExpiredException::new);

            String refreshToken = foundTokenInfo.getRefreshToken();
            jwtTokenProvider.validateToken(refreshToken);

            Long memberId = Long.valueOf(foundTokenInfo.getId());
            memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);

            //새로 발급한 Access Token 로 Redis 업데이트
            String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
            refreshTokenRepository.save(new RefreshToken(String.valueOf(memberId), refreshToken, accessToken));

            // 클라이언트 측 쿠키의 Access Token 업데이트
            CookieGenerator cookieGenerator = new CookieGenerator();
            cookieGenerator.setCookieName("token");
            cookieGenerator.setCookieHttpOnly(true);
            cookieGenerator.addCookie(response, newAccessToken);
            cookieGenerator.setCookieMaxAge(60 * 60); //1시간

            return true;
        }
    }


    private boolean isSwaggerRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("swagger") || uri.contains("api-docs") || uri.contains("webjars");
    }

    private boolean isPreflight(HttpServletRequest request) {
        return request.getMethod().equals(HttpMethod.OPTIONS.toString());
    }
}
