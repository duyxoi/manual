package nhom8.example.quizz.service;

import jakarta.transaction.Transactional;
import nhom8.example.quizz.api.dto.ExamDtos;
import nhom8.example.quizz.api.dto.ResultDtos;
import nhom8.example.quizz.api.exception.ApiException;
import nhom8.example.quizz.domain.*;
import nhom8.example.quizz.repository.AnswerRepository;
import nhom8.example.quizz.repository.ExamRepository;
import nhom8.example.quizz.repository.QuestionRepository;
import nhom8.example.quizz.repository.ResultRepository;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final AuthContextService authContextService;
    private final ExamService examService;

    public ResultService(ResultRepository resultRepository,
                         AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         AuthContextService authContextService,
                         ExamService examService) {
        this.resultRepository = resultRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.authContextService = authContextService;
        this.examService = examService;
    }

    @Transactional
    public Page<Result> listMyResults(Integer page, Integer limit) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        int p = page != null && page > 0 ? page - 1 : 0;
        int l = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "completedAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return resultRepository.findByUser_Id(ctx.userId(), pageable);
    }

    @Transactional
    public Map<String, Object> getResultDetail(Integer resultId) {
        AuthUserContext ctx = authContextService.requireCurrentUser();

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy result", null));

        if (!"admin".equals(ctx.role()) && !result.getUser().getId().equals(ctx.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không có quyền truy cập", null);
        }

        ExamDtos.ExamDto examDto = examService.getExamDto(result.getExam().getId());

        List<Answer> answers = answerRepository.findByResult_Id(result.getId());
        List<Map<String, Object>> answerDtos = answers.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("question_id", a.getQuestion().getId());
            map.put("selected_option", indexToOptionKey(a.getUserSelectedOptionIndex()));
            map.put("correct_option", indexToOptionKey(a.getCorrectOptionIndex()));
            map.put("is_correct", a.isCorrect());
            map.put("marked_for_review", a.isMarkedForReview());
            map.put("time_spent_seconds", a.getTimeSpentSeconds());
            return map;
        }).toList();

        Map<String, Object> resultObj = new HashMap<>();
        resultObj.put("result_id", result.getId());
        resultObj.put("exam_id", result.getExam().getId());
        resultObj.put("score", result.getScore());
        resultObj.put("max_score", result.getMaxScore());
        resultObj.put("correct_count", result.getCorrectCount());
        resultObj.put("wrong_count", result.getWrongCount());
        resultObj.put("total_questions", result.getTotalQuestions());
        resultObj.put("time_spent_seconds", result.getTimeSpentSeconds());
        resultObj.put("started_at", result.getStartedAt());
        resultObj.put("completed_at", result.getCompletedAt());

        Map<String, Object> response = new HashMap<>();
        response.put("result", resultObj);
        response.put("answers", answerDtos);
        response.put("exam", examDto);
        return response;
    }

    @Transactional
    public Page<Result> listResultsByExam(Integer examId, Integer page, Integer limit) {
        authContextService.requireAdmin();
        int p = page != null && page > 0 ? page - 1 : 0;
        int l = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "completedAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return resultRepository.findByExam_Id(examId, pageable);
    }

    @Transactional
    public Map<String, Object> getReview(Integer resultId) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy result", null));

        if (!"admin".equals(ctx.role()) && !result.getUser().getId().equals(ctx.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không có quyền truy cập", null);
        }

        Exam exam = result.getExam();
        ExamDtos.ExamDto examDto = examService.getExamDto(exam.getId());

        Map<Integer, Answer> answerByQuestion = new HashMap<>();
        for (Answer a : answerRepository.findByResult_Id(resultId)) {
            answerByQuestion.put(a.getQuestion().getId(), a);
        }

        List<Question> questions = questionRepository.findByExam_IdOrderByQuestionNumberAsc(exam.getId());

        List<Map<String, Object>> questionsWithAnswers = new ArrayList<>();
        int correctCount = 0;
        int wrongCount = 0;

        for (Question q : questions) {
            Answer a = answerByQuestion.get(q.getId());
            boolean isCorrect = a != null && a.isCorrect();
            if (isCorrect) correctCount++;
            else if (a != null) wrongCount++;

            Map<String, Object> qwa = new HashMap<>();
            qwa.put("question_id", q.getId());
            qwa.put("question_text", q.getQuestionText());
            qwa.put("explanation", q.getExplanation());
            qwa.put("selected_option", a != null ? indexToOptionKey(a.getUserSelectedOptionIndex()) : null);
            qwa.put("correct_option", indexToOptionKey(q.getCorrectOptionIndex()));
            qwa.put("is_correct", isCorrect);
            qwa.put("marked_for_review", a != null && a.isMarkedForReview());
            qwa.put("time_spent_seconds", a != null ? a.getTimeSpentSeconds() : null);
            questionsWithAnswers.add(qwa);
        }

        BigDecimal score = result.getScore() != null ? result.getScore() : BigDecimal.ZERO;
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("correct_count", correctCount);
        statistics.put("wrong_count", wrongCount);
        statistics.put("score", score);

        Map<String, Object> resultObj = new HashMap<>();
        resultObj.put("result_id", result.getId());
        resultObj.put("score", result.getScore());
        resultObj.put("max_score", result.getMaxScore());
        resultObj.put("correct_count", result.getCorrectCount());
        resultObj.put("wrong_count", result.getWrongCount());
        resultObj.put("total_questions", result.getTotalQuestions());

        Map<String, Object> response = new HashMap<>();
        response.put("result", resultObj);
        response.put("questions_with_answers", questionsWithAnswers);
        response.put("statistics", statistics);
        response.put("exam", examDto);
        return response;
    }

    private String indexToOptionKey(Integer idx) {
        if (idx == null) return null;
        return switch (idx) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "C";
            case 3 -> "D";
            default -> null;
        };
    }
}