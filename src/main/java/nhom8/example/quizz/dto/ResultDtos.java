package nhom8.example.quizz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ResultDtos {

    public record ResultSummaryDto(
            Integer resultId,
            @JsonProperty("exam_id") Integer examId,
            Integer score,
            @JsonProperty("started_at") LocalDateTime startedAt,
            @JsonProperty("completed_at") LocalDateTime completedAt
    ) {}

    public record AnswerReviewDto(
            @JsonProperty("question_id") Integer questionId,
            String questionText,
            @JsonProperty("selected_option") String selectedOption,
            @JsonProperty("correct_option") String correctOption,
            boolean is_correct,
            @JsonProperty("marked_for_review") boolean markedForReview,
            @JsonProperty("time_spent_seconds") Integer timeSpentSeconds,
            String explanation
    ) {}

    public record ResultDetailDto(
            @JsonProperty("result") Object result,
            @JsonProperty("answers") List<AnswerReviewDto> answers,
            @JsonProperty("exam") ExamDtos.ExamDto exam
    ) {}

    public record ReviewStatisticsDto(
            int correctCount,
            int wrongCount,
            BigDecimal score
    ) {}

    public record ReviewResponseDto(
            ExamDtos.ExamDto exam,
            Object result,
            @JsonProperty("questions_with_answers") List<AnswerReviewDto> questionsWithAnswers,
            ReviewStatisticsDto statistics
    ) {}
}

