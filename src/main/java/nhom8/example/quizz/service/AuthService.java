package nhom8.example.quizz.service;

import nhom8.example.quizz.api.dto.AuthDtos;
import nhom8.example.quizz.api.exception.ApiException;
import nhom8.example.quizz.domain.AppUser;
import nhom8.example.quizz.repository.UserRepository;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import nhom8.example.quizz.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthContextService authContextService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService, AuthContextService authContextService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authContextService = authContextService;
    }

    public AuthDtos.UserAuthData register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "Email đã tồn tại", null);
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "Username đã tồn tại", null);
        }

        AppUser user = new AppUser();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setUsername(req.username());
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setSchoolName(req.schoolName());
        user.setBio(null);
        user.setAvatarUrl(null);
        user.setRole(AppUser.Role.student);
        user.setStatus(AppUser.Status.active);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);
        user.setLastLoginAt(null);
        user.setDateOfBirth(req.dateOfBirth());
        user.setGender(parseGender(req.gender()));

        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getRole().name());
        return new AuthDtos.UserAuthData(toUserDto(saved), token);
    }

    public AuthDtos.UserAuthData login(AuthDtos.LoginRequest req) {
        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Email hoặc mật khẩu không đúng", null));

        boolean matched = passwordEncoder.matches(req.password(), user.getPasswordHash());
        if (!matched) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Email hoặc mật khẩu không đúng", null);
        }

        user.setLastLoginAt(LocalDateTime.now());
        AppUser saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getRole().name());
        return new AuthDtos.UserAuthData(toUserDto(saved), token);
    }

    public AuthDtos.UserDto me() {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));
        return toUserDto(user);
    }

    private AppUser.Gender parseGender(String gender) {
        if (gender == null) return null;
        String normalized = gender.trim();
        // JWT/DB lưu enum dạng chuỗi đúng giá trị trong code.
        return switch (normalized.toLowerCase(Locale.ROOT)) {
            case "male" -> AppUser.Gender.Male;
            case "female" -> AppUser.Gender.Female;
            case "other" -> AppUser.Gender.Other;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Giới tính không hợp lệ", null);
        };
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