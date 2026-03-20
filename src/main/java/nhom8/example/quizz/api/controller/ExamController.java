package nhom8.example.quizz.api.controller;

import nhom8.example.quizz.api.dto.ExamDtos;
import nhom8.example.quizz.api.response.ApiResponse;
import nhom8.example.quizz.api.response.PaginationDto;
import nhom8.example.quizz.domain.Exam;
import nhom8.example.quizz.service.ExamService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    // ========== Exam Management ==========
    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listExams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String difficulty
    ) {
        Page<Exam> p = examService.listExams(page, limit, subject, difficulty);

        PaginationDto paginationDto = new PaginationDto(p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());
        List<ExamDtos.ExamDto> exams = p.getContent().stream().map(ex -> examService.getExamDto(ex.getId())).toList();

        return ResponseEntity.ok(
                ApiResponse.success("Thao tác thành công", Map.of("exams", exams), paginationDto)
        );
    }

    @GetMapping("/exams/{exam_id}")
    public ResponseEntity<ApiResponse<Object>> getExam(@PathVariable("exam_id") Integer examId) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", java.util.Map.of("exam", examService.getExamDto(examId))));
    }

    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<Object>> createExam(@RequestBody ExamDtos.CreateExamRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo đề thi thành công", java.util.Map.of("exam", examService.createExam(req))));
    }

    @PutMapping("/exams/{exam_id}")
    public ResponseEntity<ApiResponse<Object>> updateExam(@PathVariable("exam_id") Integer examId,
                                                          @RequestBody ExamDtos.UpdateExamRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", java.util.Map.of("exam", examService.updateExam(examId, req))));
    }

    @DeleteMapping("/exams/{exam_id}")
    public ResponseEntity<ApiResponse<Object>> deleteExam(@PathVariable("exam_id") Integer examId) {
        examService.deleteExam(examId);
        return ResponseEntity.ok(ApiResponse.success("Xóa đề thi thành công", null));
    }

    @GetMapping("/exams/subject/{subject}")
    public ResponseEntity<ApiResponse<Object>> listExamsBySubject(@PathVariable String subject) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", java.util.Map.of("exams", examService.listExamsBySubject(subject))));
    }

    // ========== Question Management ==========
    @GetMapping("/exams/{exam_id}/questions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listQuestions(@PathVariable("exam_id") Integer examId) {
        List<ExamDtos.QuestionDto> questions = examService.listQuestions(examId);
        ExamDtos.ExamDto exam = examService.getExamDto(examId);
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", Map.of("questions", questions, "exam", exam)));
    }

    @PostMapping("/exams/{exam_id}/questions")
    public ResponseEntity<ApiResponse<Object>> createQuestion(@PathVariable("exam_id") Integer examId,
                                                               @RequestBody ExamDtos.QuestionCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo câu hỏi thành công", java.util.Map.of("question", examService.createQuestion(examId, req))));
    }

    @PutMapping("/questions/{question_id}")
    public ResponseEntity<ApiResponse<Object>> updateQuestion(@PathVariable("question_id") Integer questionId,
                                                                @RequestBody ExamDtos.QuestionUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", java.util.Map.of("question", examService.updateQuestion(questionId, req))));
    }

    @DeleteMapping("/questions/{question_id}")
    public ResponseEntity<ApiResponse<Object>> deleteQuestion(@PathVariable("question_id") Integer questionId) {
        examService.deleteQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu hỏi thành công", null));
    }
}

