package nhom8.example.quizz.controller;

import nhom8.example.quizz.dto.TakingExamDtos;
import nhom8.example.quizz.api.response.ApiResponse;
import nhom8.example.quizz.service.ExamTakingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ExamTakingController {

    private final ExamTakingService examTakingService;

    public ExamTakingController(ExamTakingService examTakingService) {
        this.examTakingService = examTakingService;
    }

    @PostMapping("/exams/{exam_id}/start")
    public ResponseEntity<ApiResponse<Object>> start(@PathVariable("exam_id") Integer examId) {
        TakingExamDtos.ExamSessionDto session = examTakingService.startExam(examId);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu thi thành công", java.util.Map.of("exam_session", session)));
    }

    @GetMapping("/exam-sessions/{session_id}/question/{question_number}")
    public ResponseEntity<ApiResponse<TakingExamDtos.NextQuestionResponse>> getQuestion(
            @PathVariable("session_id") String sessionId,
            @PathVariable("question_number") Integer questionNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", examTakingService.getNextQuestion(sessionId, questionNumber)));
    }

    @PostMapping("/exam-sessions/{session_id}/answer")
    public ResponseEntity<ApiResponse<Object>> answer(
            @PathVariable("session_id") String sessionId,
            @RequestBody TakingExamDtos.AnswerRequest req
    ) {
        examTakingService.answerQuestion(sessionId, req.questionId(), req.selectedOption(), req.markedForReview());
        return ResponseEntity.ok(ApiResponse.success("Đã lưu câu trả lời", null));
    }

    @PutMapping("/exam-sessions/{session_id}/mark-review/{question_id}")
    public ResponseEntity<ApiResponse<Object>> markReview(
            @PathVariable("session_id") String sessionId,
            @PathVariable("question_id") Integer questionId,
            @RequestBody TakingExamDtos.MarkReviewRequest req
    ) {
        examTakingService.markReview(sessionId, questionId, req.markedForReview());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu", null));
    }

    @PostMapping("/exam-sessions/{session_id}/submit")
    public ResponseEntity<ApiResponse<Object>> submit(@PathVariable("session_id") String sessionId) {
        TakingExamDtos.SubmitResultDto result = examTakingService.submit(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", java.util.Map.of("result", result)));
    }

    @GetMapping("/exam-sessions/{session_id}/progress")
    public ResponseEntity<ApiResponse<TakingExamDtos.ProgressDto>> progress(@PathVariable("session_id") String sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", examTakingService.getProgress(sessionId)));
    }
}

