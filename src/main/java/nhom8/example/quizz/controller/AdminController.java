package nhom8.example.quizz.controller;

import nhom8.example.quizz.dto.AdminDtos;
import nhom8.example.quizz.response.ApiResponse;
import nhom8.example.quizz.response.PaginationDto;
import nhom8.example.quizz.entity.AppUser;
import nhom8.example.quizz.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        Page<AppUser> p = adminService.listUsers(page, limit, role, status);
        PaginationDto paginationDto = new PaginationDto(p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());

        var users = p.getContent().stream().map(u -> Map.of(
                "id", u.getId(),
                "full_name", u.getFullName(),
                "email", u.getEmail(),
                "username", u.getUsername(),
                "phone", u.getPhone(),
                "gender", u.getGender() != null ? u.getGender().name() : null,
                "date_of_birth", u.getDateOfBirth(),
                "school_name", u.getSchoolName(),
                "role", u.getRole() != null ? u.getRole().name() : null,
                "status", u.getStatus() != null ? u.getStatus().name() : null
        )).toList();

        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", Map.of("users", users), paginationDto));
    }

    @GetMapping("/users/{user_id}")
    public ResponseEntity<ApiResponse<Object>> getUser(@PathVariable("user_id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", adminService.getUserDetails(userId)));
    }

    @PutMapping("/users/{user_id}")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @PathVariable("user_id") Integer userId,
            @RequestBody AdminDtos.UpdateUserRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", Map.of("user", adminService.updateUser(userId, req))));
    }

    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable("user_id") Integer userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa user thành công", null));
    }

    @PostMapping("/users/{user_id}/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@PathVariable("user_id") Integer userId,
                                                             @RequestBody AdminDtos.ResetPasswordRequest req) {
        adminService.resetPassword(userId, req);
        return ResponseEntity.ok(ApiResponse.success("Reset mật khẩu thành công", null));
    }
}

