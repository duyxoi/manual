package nhom8.example.quizz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ExamDtos {

    public record ExamDto(
            Integer id,
            String title,
            String description,
            String subject,
            String subjectColor,
            Integer totalQuestions,
            Integer durationMinutes,
            String difficulty,
            BigDecimal passScore,
            String status,
            Integer attemptCount,
            BigDecimal averageScore,
            String bannerColor,
            Integer createdByAdminId
    ) {
    }

    public record CreateExamRequest(
            String title,
            String description,
            String subject,
            @JsonProperty("total_questions") Integer totalQuestions,
            @JsonProperty("duration_minutes") Integer durationMinutes,
            String difficulty,
            @JsonProperty("pass_score") BigDecimal passScore

    ) {
    }

    public record UpdateExamRequest(
            String title,
            String description,
            String status
    ) {
    }

    public record OptionDto(
            String key,
            String text,
            boolean is_correct
    ) {
    }

    public record QuestionCreateRequest(
            @JsonProperty("question_text") String questionText,
            @JsonProperty("question_number") Integer questionNumber,
            List<OptionDto> options,
            String explanation
    ) {
    }

    public record QuestionUpdateRequest(
            @JsonProperty("question_text") String questionText,
            List<OptionDto> options,
            String explanation
    ) {
    }

    public record QuestionDto(
            Integer id,
            String questionText,
            Integer questionNumber,
            String explanation,
            List<OptionDto> options
    ) {
    }

    public record CreateQuestionResponse(
            String message,
            QuestionDto question
    ) {
    }
}

