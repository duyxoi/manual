package nhom8.example.quizz.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter đọc token JWT từ header Authorization và gắn vào request attribute
 * Controller/service sẽ tự kiểm tra token còn hiệu lực
 */
@Component
public class JwtAuthFilter implements Filter {

    public static final String AUTH_USER_ATTR = "authUser";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest http)) {
            chain.doFilter(request, response);
            return;
        }

        String path = http.getServletPath();

        if (path != null && (
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/login") ||
                path.startsWith("/api/openapi")
        )) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = http.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            AuthUserContext authUser = jwtService.toAuthUser(token);
            if (authUser != null) {
                http.setAttribute(AUTH_USER_ATTR, authUser);
            }
        }

        chain.doFilter(request, response);
    }
}

