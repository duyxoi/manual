package nhom8.example.quizz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

public class AuthDtos {

    public record RegisterRequest(
            @JsonProperty("full_name") String fullName,
            String email,
            String password,
            String username,
            String phone,
            String gender,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            @JsonProperty("school_name") String schoolName
    ) {
    }

    public record LoginRequest(
            String email,
            String password
    ) {
    }

    public record UserDto(
            Integer id,
            String fullName,
            String email,
            String username,
            String phone,
            String gender,
            LocalDate dateOfBirth,
            String schoolName,
            String bio,
            String avatarUrl,
            String role,
            String status
    ) {
    }

    public record UserAuthData(
            UserDto user,
            String token
    ) {
    }
}

