package nhom8.example.quizz.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StatisticsDtos {

    public record SubjectStatDto(
            String subject,
            @JsonProperty("total_exams") int totalExams,
            @JsonProperty("average_score") BigDecimal averageScore,
            @JsonProperty("highest_score") BigDecimal highestScore,
            @JsonProperty("last_exam_date") LocalDate lastExamDate
    ) {}

    public record ExamHistoryDto(
            @JsonProperty("exam_id") int examId,
            String title,
            BigDecimal score,
            @JsonProperty("completed_at") LocalDateTime completedAt
    ) {}

    public record UserStatisticsResponseDto(
            Object statistics,
            List<SubjectStatDto> subjectStats,
            List<ResultDtos.ResultSummaryDto> recentResults
    ) {}

    public record SubjectStatisticsBySubjectResponseDto(
            Object subject_statistics,
            List<ExamHistoryDto> exam_history
    ) {}
}

