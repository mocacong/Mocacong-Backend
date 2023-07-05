package mocacong.server.security.auth;

import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.BlankTokenException;
import mocacong.server.exception.unauthorized.InvalidBearerException;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@NoArgsConstructor
public class AuthorizationExtractor {

    private static final String AUTHENTICATION_TYPE = "Bearer";
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final int TOKEN_INDEX = 1;

    public static String extractAccessToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(AUTHORIZATION_HEADER_KEY);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.toLowerCase().startsWith(AUTHENTICATION_TYPE.toLowerCase())) {
                String[] parts = value.split(" ");
                if (parts.length > 1) {
                    return value.split(" ")[TOKEN_INDEX];
                } else {
                    throw new BlankTokenException();
                }
            }
        }
        throw new InvalidBearerException();
    }
}
