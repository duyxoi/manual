package nhom8.example.quizz.api.controller;

import nhom8.example.quizz.api.response.ApiResponse;
import nhom8.example.quizz.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/users/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myStatistics() {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", statisticsService.getMyStatistics()));
    }

    @GetMapping("/users/statistics/subject/{subject}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myStatisticsBySubject(@PathVariable String subject) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", statisticsService.getMyStatisticsBySubject(subject)));
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", statisticsService.getAdminStatistics(startDate, endDate)));
    }

    @GetMapping("/admin/daily-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminDailyActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", statisticsService.getAdminDailyActivity(date)));
    }
}

