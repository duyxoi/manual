package nhom8.example.quizz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class TakingExamDtos {

    public record ExamSessionDto(
            @JsonProperty("session_id") String sessionId,
            ExamDtos.ExamDto exam,
            @JsonProperty("started_at") LocalDateTime startedAt,
            @JsonProperty("time_remaining") long timeRemaining,
            List<QuestionDtos.QuestionDto> questions
    ) {
    }

    public record AnswerRequest(
            @JsonProperty("question_id") Integer questionId,
            @JsonProperty("selected_option") String selectedOption,
            @JsonProperty("marked_for_review") boolean markedForReview
    ) {
    }

    public record MarkReviewRequest(
            @JsonProperty("marked_for_review") boolean markedForReview
    ) {
    }

    public record NextQuestionResponse(
            ExamDtos.QuestionDto question,
            @JsonProperty("time_remaining") long timeRemaining
    ) {
    }

    public record ProgressDto(
            int answered,
            int marked,
            int remaining,
            @JsonProperty("time_remaining") long timeRemaining
    ) {
    }

    public record SubmitResultDto(
            Integer resultId,
            BigDecimal score,
            BigDecimal maxScore,
            int correctCount,
            int wrongCount,
            int totalQuestions,
            int timeSpentSeconds
    ) {
    }
}

