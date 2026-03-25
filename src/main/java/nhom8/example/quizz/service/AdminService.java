package nhom8.example.quizz.service;

import jakarta.transaction.Transactional;
import nhom8.example.quizz.dto.AuthDtos;
import nhom8.example.quizz.dto.AdminDtos;
import nhom8.example.quizz.exception.ApiException;
import nhom8.example.quizz.entity.AppUser;
import nhom8.example.quizz.entity.UserStatistics;
import nhom8.example.quizz.entity.SubjectStatistics;
import nhom8.example.quizz.repository.*;
import nhom8.example.quizz.security.AuthContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final SubjectStatisticsRepository subjectStatisticsRepository;
    private final AuthContextService authContextService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(UserRepository userRepository,
                        UserStatisticsRepository userStatisticsRepository,
                        SubjectStatisticsRepository subjectStatisticsRepository,
                        AuthContextService authContextService) {
        this.userRepository = userRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.subjectStatisticsRepository = subjectStatisticsRepository;
        this.authContextService = authContextService;
    }

    public Page<AppUser> listUsers(Integer page, Integer limit, String role, String status) {
        authContextService.requireAdmin();
        int p = page != null && page > 0 ? page - 1 : 0;
        int l = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "id"));

        AppUser.Role r = role != null && !role.isBlank() ? parseRole(role) : null;
        AppUser.Status s = status != null && !status.isBlank() ? parseStatus(status) : null;

        if (r != null && s != null) return userRepository.findByRoleAndStatus(r, s, pageable);
        if (r != null) return userRepository.findByRole(r, pageable);
        if (s != null) return userRepository.findByStatus(s, pageable);
        return userRepository.findAll(pageable);
    }

    public Map<String, Object> getUserDetails(Integer userId) {
        authContextService.requireAdmin();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        Optional<UserStatistics> usOpt = userStatisticsRepository.findByUser_Id(userId);
        List<SubjectStatistics> subjectStats = subjectStatisticsRepository.findByUser_Id(userId);

        Map<String, Object> statistics = usOpt.map(us -> {
            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("total_exams_taken", us.getTotalExamsTaken());
            statsMap.put("average_score", us.getAverageScore());
            statsMap.put("highest_score", us.getHighestScore());
            statsMap.put("total_correct", us.getTotalCorrect());
            statsMap.put("total_wrong", us.getTotalWrong());
            statsMap.put("total_time_spent_hours", us.getTotalTimeSpentHours());
            statsMap.put("ranking", us.getRanking());
            statsMap.put("streak_days", us.getStreakDays());
            statsMap.put("last_exam_date", us.getLastExamDate());
            return statsMap;
        }).orElse(null);

        List<Map<String, Object>> subjectStatsDto = subjectStats.stream().map(ss -> {
            Map<String, Object> ssMap = new HashMap<>();
            ssMap.put("subject", ss.getSubject());
            ssMap.put("total_exams", ss.getTotalExams());
            ssMap.put("average_score", ss.getAverageScore());
            ssMap.put("highest_score", ss.getHighestScore());
            ssMap.put("last_exam_date", ss.getLastExamDate());
            return ssMap;
        }).toList();

        Map<String, Object> statsWrapper = new HashMap<>();
        statsWrapper.put("user_statistics", statistics);
        statsWrapper.put("subject_statistics", subjectStatsDto);

        Map<String, Object> response = new HashMap<>();
        response.put("user", toUserDto(user));
        response.put("statistics", statsWrapper);

        return response;
    }

    @Transactional
    public AuthDtos.UserDto updateUser(Integer userId, AdminDtos.UpdateUserRequest req) {
        authContextService.requireAdmin();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        if (req.status() != null && !req.status().isBlank()) {
            user.setStatus(parseStatus(req.status()));
        }
        if (req.role() != null && !req.role().isBlank()) {
            user.setRole(parseRole(req.role()));
        }

        AppUser saved = userRepository.save(user);
        return toUserDto(saved);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        authContextService.requireAdmin();
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Integer userId, AdminDtos.ResetPasswordRequest req) {
        authContextService.requireAdmin();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));

        if (req.newPassword() == null || req.newPassword().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "new_password phải >= 6 ký tự", null);
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private AppUser.Role parseRole(String role) {
        String norm = role.trim().toLowerCase();
        return switch (norm) {
            case "student" -> AppUser.Role.student;
            case "admin" -> AppUser.Role.admin;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "role không hợp lệ", null);
        };
    }

    private AppUser.Status parseStatus(String status) {
        String norm = status.trim().toLowerCase();
        return switch (norm) {
            case "active" -> AppUser.Status.active;
            case "inactive" -> AppUser.Status.inactive;
            case "locked" -> AppUser.Status.locked;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "status không hợp lệ", null);
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