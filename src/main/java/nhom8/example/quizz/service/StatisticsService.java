package nhom8.example.quizz.service;

import jakarta.transaction.Transactional;
import nhom8.example.quizz.api.exception.ApiException;
import nhom8.example.quizz.api.response.PaginationDto;
import nhom8.example.quizz.api.dto.ResultDtos;
import nhom8.example.quizz.domain.*;
import nhom8.example.quizz.repository.*;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatisticsService {

    private final ResultRepository resultRepository;
    private final AnswerRepository answerRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final SubjectStatisticsRepository subjectStatisticsRepository;
    private final AdminStatisticsRepository adminStatisticsRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final ExamService examService;
    private final AuthContextService authContextService;

    public StatisticsService(ResultRepository resultRepository,
                             AnswerRepository answerRepository,
                             UserStatisticsRepository userStatisticsRepository,
                             SubjectStatisticsRepository subjectStatisticsRepository,
                             AdminStatisticsRepository adminStatisticsRepository,
                             DailyActivityRepository dailyActivityRepository,
                             ExamService examService,
                             AuthContextService authContextService) {
        this.resultRepository = resultRepository;
        this.answerRepository = answerRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.subjectStatisticsRepository = subjectStatisticsRepository;
        this.adminStatisticsRepository = adminStatisticsRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.examService = examService;
        this.authContextService = authContextService;
    }

    @Transactional
    public Map<String, Object> getMyStatistics() {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        Integer userId = ctx.userId();

        Optional<UserStatistics> stored = userStatisticsRepository.findByUser_Id(userId);
        List<SubjectStatistics> subjectStatsStored = subjectStatisticsRepository.findByUser_Id(userId);
        List<Result> recent = resultRepository.findByUser_IdOrderByCompletedAtDesc(userId);

        List<Map<String, Object>> recentResults = recent.stream().limit(5).map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("result_id", r.getId());
            map.put("exam", examService.getExamDto(r.getExam().getId()));
            map.put("score", r.getScore());
            map.put("completed_at", r.getCompletedAt());
            return map;
        }).toList();

        if (stored.isPresent()) {
            UserStatistics us = stored.get();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total_exams_taken", us.getTotalExamsTaken());
            statistics.put("average_score", us.getAverageScore());
            statistics.put("highest_score", us.getHighestScore());
            statistics.put("total_correct", us.getTotalCorrect());
            statistics.put("total_wrong", us.getTotalWrong());
            statistics.put("total_time_spent_hours", us.getTotalTimeSpentHours());
            statistics.put("ranking", us.getRanking());
            statistics.put("streak_days", us.getStreakDays());
            statistics.put("last_exam_date", us.getLastExamDate());

            List<Map<String, Object>> subjectStats = subjectStatsStored.stream().map(ss -> {
                Map<String, Object> map = new HashMap<>();
                map.put("subject", ss.getSubject());
                map.put("total_exams", ss.getTotalExams());
                map.put("average_score", ss.getAverageScore());
                map.put("highest_score", ss.getHighestScore());
                map.put("last_exam_date", ss.getLastExamDate());
                return map;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("statistics", statistics);
            response.put("subject_stats", subjectStats);
            response.put("recent_results", recentResults);
            return response;
        }

        return computeAndReturnUserStats(userId, recentResults);
    }

    @Transactional
    public Map<String, Object> getMyStatisticsBySubject(String subject) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        Integer userId = ctx.userId();

        Optional<SubjectStatistics> stored = subjectStatisticsRepository.findByUser_IdAndSubject(userId, subject);
        List<Result> results = resultRepository.findByUser_IdAndExam_SubjectIgnoreCaseOrderByCompletedAtDesc(userId, subject);

        List<Map<String, Object>> examHistory = results.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("exam_id", r.getExam().getId());
            map.put("title", r.getExam().getTitle());
            map.put("score", r.getScore());
            map.put("completed_at", r.getCompletedAt());
            return map;
        }).toList();

        if (stored.isPresent()) {
            SubjectStatistics ss = stored.get();
            Map<String, Object> ssMap = new HashMap<>();
            ssMap.put("subject", ss.getSubject());
            ssMap.put("total_exams", ss.getTotalExams());
            ssMap.put("average_score", ss.getAverageScore());
            ssMap.put("highest_score", ss.getHighestScore());
            ssMap.put("last_exam_date", ss.getLastExamDate());

            Map<String, Object> response = new HashMap<>();
            response.put("subject_statistics", ssMap);
            response.put("exam_history", examHistory);
            return response;
        }

        if (results.isEmpty()) {
            Map<String, Object> emptySsMap = new HashMap<>();
            emptySsMap.put("subject", subject);
            emptySsMap.put("total_exams", 0);
            emptySsMap.put("average_score", null);
            emptySsMap.put("highest_score", null);
            emptySsMap.put("last_exam_date", null);

            Map<String, Object> response = new HashMap<>();
            response.put("subject_statistics", emptySsMap);
            response.put("exam_history", examHistory);
            return response;
        }

        int totalExams = results.size();
        BigDecimal avg = results.stream().map(Result::getScore).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalExams), 2, RoundingMode.HALF_UP);
        BigDecimal highest = results.stream().map(Result::getScore).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        LocalDate last = results.stream().map(r -> r.getCompletedAt() != null ? r.getCompletedAt().toLocalDate() : null)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        Map<String, Object> computedSsMap = new HashMap<>();
        computedSsMap.put("subject", subject);
        computedSsMap.put("total_exams", totalExams);
        computedSsMap.put("average_score", avg);
        computedSsMap.put("highest_score", highest);
        computedSsMap.put("last_exam_date", last);

        Map<String, Object> response = new HashMap<>();
        response.put("subject_statistics", computedSsMap);
        response.put("exam_history", examHistory);
        return response;
    }

    @Transactional
    public Map<String, Object> getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        authContextService.requireAdmin();

        LocalDate from = startDate != null ? startDate : LocalDate.now().minusDays(7);
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        if (from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "startDate không được lớn hơn endDate", null);
        }

        List<AdminStatistics> stats = adminStatisticsRepository.findByStatDateBetween(from, to);

        Map<String, Object> systemStats = new HashMap<>();
        if (!stats.isEmpty()) {
            AdminStatistics latest = stats.stream().max(Comparator.comparing(AdminStatistics::getStatDate)).orElse(null);
            if (latest != null) {
                systemStats.put("latest_stat_date", latest.getStatDate());
                systemStats.put("total_users", latest.getTotalUsers());
                systemStats.put("active_users", latest.getActiveUsers());
                systemStats.put("locked_users", latest.getLockedUsers());
                systemStats.put("new_registrations", latest.getNewRegistrations());
                systemStats.put("total_exams", latest.getTotalExams());
                systemStats.put("published_exams", latest.getPublishedExams());
                systemStats.put("draft_exams", latest.getDraftExams());
                systemStats.put("archived_exams", latest.getArchivedExams());
                systemStats.put("total_attempts", latest.getTotalAttempts());
                systemStats.put("average_score", latest.getAverageScore());
                systemStats.put("pass_rate_percent", latest.getPassRatePercent());
                systemStats.put("peak_hour", latest.getPeakHour());
            }
        }

        // daily_activity theo từng ngày trong range
        List<Map<String, Object>> dailyActivity = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            for (DailyActivity da : dailyActivityRepository.findByActivityDate(d)) {
                Map<String, Object> daMap = new HashMap<>();
                daMap.put("activity_date", da.getActivityDate());
                daMap.put("hour_of_day", da.getHourOfDay());
                daMap.put("exam_attempts", da.getExamAttempts());
                daMap.put("active_users", da.getActiveUsers());
                daMap.put("total_score_sum", da.getTotalScoreSum());
                dailyActivity.add(daMap);
            }
        }

        Map<String, Object> chartsData = new HashMap<>();
        chartsData.put("admin_statistics", stats);

        Map<String, Object> response = new HashMap<>();
        response.put("system_stats", systemStats);
        response.put("daily_activity", dailyActivity);
        response.put("charts_data", chartsData);
        return response;
    }

    @Transactional
    public Map<String, Object> getAdminDailyActivity(LocalDate date) {
        authContextService.requireAdmin();
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "date không được rỗng", null);
        }

        List<DailyActivity> rows = dailyActivityRepository.findByActivityDate(date);
        int examAttempts = rows.stream().mapToInt(DailyActivity::getExamAttempts).sum();
        int activeUsers = rows.stream().mapToInt(DailyActivity::getActiveUsers).sum();
        BigDecimal sumScore = rows.stream()
                .map(DailyActivity::getTotalScoreSum)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> activity = rows.stream().map(da -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity_date", da.getActivityDate());
            map.put("hour_of_day", da.getHourOfDay());
            map.put("exam_attempts", da.getExamAttempts());
            map.put("active_users", da.getActiveUsers());
            map.put("total_score_sum", da.getTotalScoreSum());
            return map;
        }).toList();

        Map<String, Object> summary = new HashMap<>();
        summary.put("exam_attempts", examAttempts);
        summary.put("active_users", activeUsers);
        summary.put("total_score_sum", sumScore);

        Map<String, Object> response = new HashMap<>();
        response.put("activity", activity);
        response.put("summary", summary);
        return response;
    }

    private Map<String, Object> computeAndReturnUserStats(Integer userId, List<Map<String, Object>> recentResults) {
        List<Result> results = resultRepository.findByUser_IdOrderByCompletedAtDesc(userId);
        int totalExamsTaken = results.size();

        BigDecimal avgScore = null;
        BigDecimal highest = null;
        if (!results.isEmpty()) {
            avgScore = results.stream().map(Result::getScore).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(totalExamsTaken), 2, RoundingMode.HALF_UP);
            highest = results.stream().map(Result::getScore).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        }

        int totalCorrect = 0;
        int totalWrong = 0;
        int totalTimeSpentSeconds = 0;
        LocalDate lastExamDate = null;

        for (Result r : results) {
            totalTimeSpentSeconds += r.getTimeSpentSeconds() != null ? r.getTimeSpentSeconds() : 0;
            if (r.getCompletedAt() != null) {
                LocalDate ld = r.getCompletedAt().toLocalDate();
                lastExamDate = lastExamDate == null ? ld : (ld.isAfter(lastExamDate) ? ld : lastExamDate);
            }

            List<Answer> answers = answerRepository.findByResult_Id(r.getId());
            for (Answer a : answers) {
                if (a.isCorrect()) totalCorrect++;
                else totalWrong++;
            }
        }

        int totalTimeSpentHours = (int) Math.round(totalTimeSpentSeconds / 3600.0);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_exams_taken", totalExamsTaken);
        statistics.put("average_score", avgScore);
        statistics.put("highest_score", highest);
        statistics.put("total_correct", totalCorrect);
        statistics.put("total_wrong", totalWrong);
        statistics.put("total_time_spent_hours", totalTimeSpentHours);
        statistics.put("ranking", null);
        statistics.put("streak_days", null);
        statistics.put("last_exam_date", lastExamDate);

        // subject_stats cơ bản theo exam.subject
        Map<String, List<Result>> bySubject = new HashMap<>();
        for (Result r : results) {
            String subject = r.getExam().getSubject();
            bySubject.computeIfAbsent(subject, k -> new ArrayList<>()).add(r);
        }

        List<Map<String, Object>> subjectStats = bySubject.entrySet().stream().map(e -> {
            List<Result> rs = e.getValue();
            int total = rs.size();
            BigDecimal avg = rs.stream().map(Result::getScore).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            BigDecimal high = rs.stream().map(Result::getScore).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
            LocalDate last = rs.stream()
                    .map(r -> r.getCompletedAt() != null ? r.getCompletedAt().toLocalDate() : null)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder()).orElse(null);

            Map<String, Object> map = new HashMap<>();
            map.put("subject", e.getKey());
            map.put("total_exams", total);
            map.put("average_score", avg);
            map.put("highest_score", high);
            map.put("last_exam_date", last);
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", statistics);
        response.put("subject_stats", subjectStats);
        response.put("recent_results", recentResults);
        return response;
    }
}