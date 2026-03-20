package nhom8.example.quizz.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class UserDtos {

    public record ProfileUpdateRequest(
            @JsonProperty("full_name") String fullName,
            String phone,
            String gender,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            @JsonProperty("school_name") String schoolName,
            String bio
    ) {
    }

    public record ChangePasswordRequest(
            @JsonProperty("current_password") String currentPassword,
            @JsonProperty("new_password") String newPassword
    ) {
    }
}

