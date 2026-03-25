package nhom8.example.quizz.security;

import jakarta.servlet.http.HttpServletRequest;
import nhom8.example.quizz.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class AuthContextService {

    private final HttpServletRequest request;

    public AuthContextService(HttpServletRequest request) {
        this.request = request;
    }

    public AuthUserContext requireCurrentUser() {
        Object attr = request.getAttribute(JwtAuthFilter.AUTH_USER_ATTR);
        if (!(attr instanceof AuthUserContext authUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa đăng nhập hoặc token không hợp lệ", null);
        }
        return authUser;
    }

    public void requireAdmin() {
        AuthUserContext authUser = requireCurrentUser();
        if (!"admin".equals(authUser.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không có quyền truy cập", null);
        }
    }
}

