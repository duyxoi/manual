package nhom8.example.quizz.controller;

import nhom8.example.quizz.dto.AuthDtos;
import nhom8.example.quizz.api.response.ApiResponse;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthContextService authContextService;

    public AuthController(AuthService authService, AuthContextService authContextService) {
        this.authService = authService;
        this.authContextService = authContextService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.UserAuthData>> register(@RequestBody AuthDtos.RegisterRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Đăng ký thành công", authService.register(req))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.UserAuthData>> login(@RequestBody AuthDtos.LoginRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", authService.login(req))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        authContextService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDtos.UserDto>> me() {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", authService.me()));
    }
}

