package nhom8.example.quizz.security;

/**
 * Thông tin user rút ra từ JWT
 */
public record AuthUserContext(Integer userId, String role) {
}

