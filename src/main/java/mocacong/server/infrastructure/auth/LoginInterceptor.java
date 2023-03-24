package mocacong.server.infrastructure.auth;

import lombok.NoArgsConstructor;
import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;

    public LoginInterceptor(final JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (isPreflight(request)) {
            return true;
        }

        try {
            String token = AuthorizationExtractor.extractAccessToken(request);
            jwtUtils.validateToken(token);
        } catch (MocacongException e) {
            response.sendError(e.getStatus().value(), e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isPreflight(HttpServletRequest request) {
        return request.getMethod().equals(HttpMethod.OPTIONS.toString());
    }
}
