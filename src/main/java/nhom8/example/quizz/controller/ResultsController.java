package nhom8.example.quizz.controller;

import nhom8.example.quizz.response.ApiResponse;
import nhom8.example.quizz.response.PaginationDto;
import nhom8.example.quizz.entity.Result;
import nhom8.example.quizz.service.ExamService;
import nhom8.example.quizz.service.ResultService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResultsController {

    private final ResultService resultService;
    private final ExamService examService;

    public ResultsController(ResultService resultService, ExamService examService) {
        this.resultService = resultService;
        this.examService = examService;
    }

    @GetMapping("/users/results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listMyResults(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        Page<Result> p = resultService.listMyResults(page, limit);
        PaginationDto paginationDto = new PaginationDto(p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());

        List<Map<String, Object>> results = p.getContent().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("result_id", r.getId());
            map.put("exam_id", r.getExam().getId());
            map.put("score", r.getScore());
            map.put("started_at", r.getStartedAt());
            map.put("completed_at", r.getCompletedAt());
            map.put("exam", examService.getExamDto(r.getExam().getId()));
            return map;
        }).toList();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("results", results);

        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", responseData, paginationDto));
    }

    @GetMapping("/results/{result_id}")
    public ResponseEntity<ApiResponse<Object>> getResultDetail(@PathVariable("result_id") Integer resultId) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", resultService.getResultDetail(resultId)));
    }

    @GetMapping("/exams/{exam_id}/results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listResultsByExam(
            @PathVariable("exam_id") Integer examId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        Page<Result> p = resultService.listResultsByExam(examId, page, limit);
        PaginationDto paginationDto = new PaginationDto(p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());

        List<Map<String, Object>> results = p.getContent().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("result_id", r.getId());
            map.put("user_id", r.getUser().getId());
            map.put("score", r.getScore());
            map.put("completed_at", r.getCompletedAt());
            return map;
        }).toList();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("results", results);
        responseData.put("exam", examService.getExamDto(examId));

        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", responseData, paginationDto));
    }

    @GetMapping("/results/{result_id}/review")
    public ResponseEntity<ApiResponse<Object>> review(@PathVariable("result_id") Integer resultId) {
        return ResponseEntity.ok(ApiResponse.success("Thao tác thành công", resultService.getReview(resultId)));
    }
}