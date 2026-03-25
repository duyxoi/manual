package nhom8.example.quizz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminDtos {

    public record UpdateUserRequest(
            String status,
            String role
    ) {}

    public record ResetPasswordRequest(
            @JsonProperty("new_password") String newPassword
    ) {}
}

