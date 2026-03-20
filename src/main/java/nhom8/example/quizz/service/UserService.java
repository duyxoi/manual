package nhom8.example.quizz.service;

import jakarta.transaction.Transactional;
import nhom8.example.quizz.api.dto.AuthDtos;
import nhom8.example.quizz.api.dto.UserDtos;
import nhom8.example.quizz.api.exception.ApiException;
import nhom8.example.quizz.domain.AppUser;
import nhom8.example.quizz.repository.UserRepository;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, AuthContextService authContextService) {
        this.userRepository = userRepository;
        this.authContextService = authContextService;
    }

    public AuthDtos.UserDto getProfile() {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));
        return toUserDto(user);
    }

    @Transactional
    public AuthDtos.UserDto updateProfile(UserDtos.ProfileUpdateRequest req) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        user.setFullName(requireNotBlank(req.fullName(), "full_name"));
        user.setPhone(req.phone());
        user.setGender(parseGender(req.gender()));
        user.setDateOfBirth(req.dateOfBirth());
        user.setSchoolName(req.schoolName());
        user.setBio(req.bio());

        AppUser saved = userRepository.save(user);
        return toUserDto(saved);
    }

    @Transactional
    public void changePassword(UserDtos.ChangePasswordRequest req) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        boolean matched = passwordEncoder.matches(req.currentPassword(), user.getPasswordHash());
        if (!matched) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Mật khẩu hiện tại không đúng", null);
        }
        if (req.newPassword() == null || req.newPassword().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "new_password phải >= 6 ký tự", null);
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(MultipartFile avatar) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        if (avatar == null || avatar.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "File avatar không hợp lệ", null);
        }

        String basePath = "uploads";
        Path dir = Paths.get(basePath);
        try {
            Files.createDirectories(dir);
            String original = avatar.getOriginalFilename() != null ? avatar.getOriginalFilename() : "avatar";
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);

            String filename = "avatar-" + user.getId() + "-" + UUID.randomUUID() + ext;
            Path filePath = dir.resolve(filename);
            Files.copy(avatar.getInputStream(), filePath);

            String url = "/uploads/" + filename;
            user.setAvatarUrl(url);
            userRepository.save(user);
            return url;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Không upload được avatar", null);
        }
    }

    private AppUser.Gender parseGender(String gender) {
        if (gender == null) return null;
        String normalized = gender.trim();
        return switch (normalized.toLowerCase(Locale.ROOT)) {
            case "male" -> AppUser.Gender.Male;
            case "female" -> AppUser.Gender.Female;
            case "other" -> AppUser.Gender.Other;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Giới tính không hợp lệ", null);
        };
    }

    private String requireNotBlank(String val, String fieldName) {
        if (val == null || val.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", fieldName + " không được rỗng", null);
        }
        return val.trim();
    }

    private AuthDtos.UserDto toUserDto(AppUser user) {
        return new AuthDtos.UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getDateOfBirth(),
                user.getSchoolName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getStatus() != null ? user.getStatus().name() : null
        );
    }
}