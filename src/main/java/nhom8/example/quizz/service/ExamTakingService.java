package nhom8.example.quizz.service;

import jakarta.transaction.Transactional;
import nhom8.example.quizz.dto.ExamDtos;
import nhom8.example.quizz.dto.QuestionDtos;
import nhom8.example.quizz.dto.TakingExamDtos;
import nhom8.example.quizz.exception.ApiException;
import nhom8.example.quizz.entity.*;
import nhom8.example.quizz.repository.UserRepository;
import nhom8.example.quizz.repository.AnswerRepository;
import nhom8.example.quizz.repository.ExamRepository;
import nhom8.example.quizz.repository.QuestionRepository;
import nhom8.example.quizz.repository.ResultRepository;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ExamTakingService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;
    private final AnswerRepository answerRepository;
    private final AuthContextService authContextService;
    private final ExamService examService;
    private final QuestionService questionService;

    public ExamTakingService(UserRepository userRepository,
                             ExamRepository examRepository,
                             QuestionRepository questionRepository,
                             ResultRepository resultRepository,
                             AnswerRepository answerRepository,
                             AuthContextService authContextService,
                             ExamService examService,
                             QuestionService questionService) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.answerRepository = answerRepository;
        this.authContextService = authContextService;
        this.examService = examService;
        this.questionService = questionService;
    }

    @Transactional
    public TakingExamDtos.ExamSessionDto startExam(Integer examId) {
        AuthUserContext ctx = authContextService.requireCurrentUser();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đề thi", null));
        AppUser user = userRepository.findById(ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy user", null));
        List<QuestionDtos.QuestionDto> listQuestion = questionService.findByExam(examId);
        LocalDateTime now = LocalDateTime.now();

        Result result = new Result();
        result.setUser(user);
        result.setExam(exam);

        result.setScore(java.math.BigDecimal.ZERO);
        result.setCorrectCount(0);
        result.setWrongCount(0);
        result.setTotalQuestions(exam.getTotalQuestions());
        result.setTimeSpentSeconds(0);
        result.setStartedAt(now);
        result.setCompletedAt(now);
        result.setMaxScore(new java.math.BigDecimal("10.00"));

        Result saved = resultRepository.save(result);

        long timeRemaining = exam.getDurationMinutes().longValue() * 60L;
        return new TakingExamDtos.ExamSessionDto(String.valueOf(saved.getId()), examService.getExamDto(examId), now, timeRemaining,listQuestion);
    }

    @Transactional
    public TakingExamDtos.NextQuestionResponse getNextQuestion(String sessionId, Integer questionNumber) {
        Result result = requireResultOwnerOrThrow(sessionId);

        Exam exam = result.getExam();
        Question question = questionRepository.findByExam_IdAndQuestionNumber(exam.getId(), questionNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy câu hỏi", null));

        long timeRemaining = computeTimeRemainingSeconds(result);
        return new TakingExamDtos.NextQuestionResponse(toQuestionDto(question), timeRemaining);
    }

    @Transactional
    public void answerQuestion(String sessionId, Integer questionId, String selectedOption, boolean markedForReview) {
        Result result = requireResultOwnerOrThrow(sessionId);
        Exam exam = result.getExam();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy câu hỏi", null));

        // Đảm bảo question thuộc đúng exam
        if (question.getExam() == null || !question.getExam().getId().equals(exam.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "question_id không thuộc exam của session", null);
        }

        int selectedIndex = optionKeyToIndex(selectedOption);
        int correctIndex = question.getCorrectOptionIndex();
        boolean isCorrect = selectedIndex == correctIndex;

        Answer answer = answerRepository.findByResult_IdAndQuestion_Id(result.getId(), questionId).orElse(null);
        if (answer == null) {
            answer = new Answer();
            answer.setResult(result);
            answer.setQuestion(question);
        }

        answer.setUserSelectedOptionIndex(selectedIndex);
        answer.setCorrectOptionIndex(correctIndex);
        answer.setCorrect(isCorrect);
        answer.setMarkedForReview(markedForReview);

        answerRepository.save(answer);
    }

    @Transactional
    public void markReview(String sessionId, Integer questionId, boolean markedForReview) {
        Result result = requireResultOwnerOrThrow(sessionId);
        Answer answer = answerRepository.findByResult_IdAndQuestion_Id(result.getId(), questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy câu trả lời", null));
        answer.setMarkedForReview(markedForReview);
        answerRepository.save(answer);
    }

    @Transactional
    public TakingExamDtos.SubmitResultDto submit(String sessionId) {
        Result result = requireResultOwnerOrThrow(sessionId);
        Exam exam = result.getExam();

        LocalDateTime now = LocalDateTime.now();
        long elapsedSeconds = Duration.between(result.getStartedAt(), now).getSeconds();

        List<Answer> answers = answerRepository.findByResult_Id(result.getId());
        int correctCount = (int) answers.stream().filter(Answer::isCorrect).count();
        int wrongCount = answers.size() - correctCount;

        int totalQuestions = exam.getTotalQuestions() != null ? exam.getTotalQuestions() : 0;
        java.math.BigDecimal maxScore = result.getMaxScore() != null ? result.getMaxScore() : new java.math.BigDecimal("10.00");
        java.math.BigDecimal score;
        if (totalQuestions <= 0) {
            score = java.math.BigDecimal.ZERO;
        } else {
            score = maxScore.multiply(java.math.BigDecimal.valueOf(correctCount))
                    .divide(java.math.BigDecimal.valueOf(totalQuestions), 2, java.math.RoundingMode.HALF_UP);
        }

        result.setScore(score);
        result.setCorrectCount(correctCount);
        result.setWrongCount(wrongCount);
        result.setTimeSpentSeconds((int) Math.max(0, elapsedSeconds));
        result.setCompletedAt(now);

        Result saved = resultRepository.save(result);
        return new TakingExamDtos.SubmitResultDto(saved.getId(), saved.getScore(), saved.getMaxScore(), correctCount, wrongCount, totalQuestions, saved.getTimeSpentSeconds());
    }

    @Transactional
    public TakingExamDtos.ProgressDto getProgress(String sessionId) {
        Result result = requireResultOwnerOrThrow(sessionId);
        int total = result.getTotalQuestions() != null ? result.getTotalQuestions() : 0;

        int answered = (int) answerRepository.countByResult_Id(result.getId());
        int marked = (int) answerRepository.countByResult_IdAndMarkedForReviewTrue(result.getId());
        int remaining = Math.max(0, total - answered);

        long timeRemaining = computeTimeRemainingSeconds(result);
        return new TakingExamDtos.ProgressDto(answered, marked, remaining, timeRemaining);
    }

    private Result requireResultOwnerOrThrow(String sessionId) {
        Integer rid;
        try {
            rid = Integer.valueOf(sessionId);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "session_id không hợp lệ", null);
        }

        AuthUserContext ctx = authContextService.requireCurrentUser();
        Result result = resultRepository.findById(rid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy session", null));

        // Cho phép admin xem mọi session, còn student thì chỉ được xem session của mình.
        if (!"admin".equals(ctx.role()) && !result.getUser().getId().equals(ctx.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không có quyền truy cập", null);
        }
        return result;
    }

    private long computeTimeRemainingSeconds(Result result) {
        long durationSeconds = result.getExam().getDurationMinutes().longValue() * 60L;
        long elapsed = Duration.between(result.getStartedAt(), LocalDateTime.now()).getSeconds();
        long remaining = durationSeconds - elapsed;
        return Math.max(0, remaining);
    }

    private ExamDtos.QuestionDto toQuestionDto(Question question) {
        List<ExamDtos.OptionDto> optionDtos = question.getOptions().stream()
                .sorted(Comparator.comparing(o -> o.getOptionKey().ordinal()))
                .map(o -> new ExamDtos.OptionDto(o.getOptionKey().name(), o.getOptionText(), o.isCorrect()))
                .toList();

        return new ExamDtos.QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionNumber(),
                question.getExplanation(),
                optionDtos
        );
    }

    private int optionKeyToIndex(String selectedOption) {
        if (selectedOption == null || selectedOption.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "selected_option không hợp lệ", null);
        }
        String norm = selectedOption.trim().toUpperCase(Locale.ROOT);
        return switch (norm) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "selected_option phải là A/B/C/D", null);
        };
    }
}