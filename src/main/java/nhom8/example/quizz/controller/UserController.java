package nhom8.example.quizz.controller;

import nhom8.example.quizz.dto.AuthDtos;
import nhom8.example.quizz.dto.UserDtos;
import nhom8.example.quizz.api.response.ApiResponse;
import nhom8.example.quizz.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthDtos.UserDto>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", userService.getProfile()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthDtos.UserDto>> updateProfile(@RequestBody UserDtos.ProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", userService.updateProfile(req)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(@RequestBody UserDtos.ChangePasswordRequest req) {
        userService.changePassword(req);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @PostMapping(value = "/upload-avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String url = userService.uploadAvatar(avatar);
        return ResponseEntity.ok(
                ApiResponse.success("Upload thành công", Map.of("avatar_url", url))
        );
    }
}

