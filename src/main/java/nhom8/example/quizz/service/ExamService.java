package nhom8.example.quizz.service;

import nhom8.example.quizz.dto.ExamDtos;
import nhom8.example.quizz.exception.ApiException;
import nhom8.example.quizz.entity.*;
import nhom8.example.quizz.repository.ExamRepository;
import nhom8.example.quizz.repository.QuestionRepository;
import nhom8.example.quizz.security.AuthContextService;
import nhom8.example.quizz.security.AuthUserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AuthContextService authContextService;

    public ExamService(ExamRepository examRepository, QuestionRepository questionRepository, AuthContextService authContextService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.authContextService = authContextService;
    }

    public Page<Exam> listExams(Integer page, Integer limit, String subject, String difficulty) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int l = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "id"));

        Exam.Difficulty diff = difficulty != null && !difficulty.isBlank() ? parseDifficulty(difficulty) : null;
        boolean hasSubject = subject != null && !subject.isBlank();

        if (hasSubject && diff != null) {
            return examRepository.findBySubjectIgnoreCaseAndDifficulty(subject, diff, pageable);
        }
        if (hasSubject) {
            return examRepository.findBySubjectIgnoreCase(subject, pageable);
        }
        if (diff != null) {
            return examRepository.findByDifficulty(diff, pageable);
        }
        return examRepository.findAll(pageable);
    }

    public ExamDtos.ExamDto getExamDto(Integer examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đề thi", null));
        return toExamDto(exam);
    }

    public ExamDtos.ExamDto createExam(ExamDtos.CreateExamRequest req) {
        authContextService.requireAdmin();
        AuthUserContext ctx = authContextService.requireCurrentUser();

        Exam exam = new Exam();
        exam.setTitle(req.title());
        exam.setDescription(req.description());
        exam.setSubject(req.subject());
        exam.setTotalQuestions(req.totalQuestions());
        exam.setDurationMinutes(req.durationMinutes());
        exam.setDifficulty(parseDifficulty(req.difficulty()));
        exam.setPassScore(req.passScore() != null ? req.passScore() : new BigDecimal("5.00"));
        exam.setStatus(Exam.Status.draft);
        exam.setAttemptCount(0);
        exam.setAverageScore(null);
        exam.setCreatedByAdminId(ctx.userId());

        Exam saved = examRepository.save(exam);
        return toExamDto(saved);
    }

    public ExamDtos.ExamDto updateExam(Integer examId, ExamDtos.UpdateExamRequest req) {
        authContextService.requireAdmin();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đề thi", null));

        if (req.title() != null) exam.setTitle(req.title());
        if (req.description() != null) exam.setDescription(req.description());
        if (req.status() != null) exam.setStatus(parseExamStatus(req.status()));

        Exam saved = examRepository.save(exam);
        return toExamDto(saved);
    }

    public void deleteExam(Integer examId) {
        authContextService.requireAdmin();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đề thi", null));
        examRepository.delete(exam);
    }

    public List<ExamDtos.ExamDto> listExamsBySubject(String subject) {
        List<Exam> exams = examRepository.findBySubjectIgnoreCase(subject);
        return exams.stream().map(this::toExamDto).toList();
    }

    public List<ExamDtos.QuestionDto> listQuestions(Integer examId) {
        // endpoint này không yêu cầu admin
        List<Question> questions = questionRepository.findByExam_IdOrderByQuestionNumberAsc(examId);
        return questions.stream().map(this::toQuestionDto).toList();
    }

    public ExamDtos.QuestionDto createQuestion(Integer examId, ExamDtos.QuestionCreateRequest req) {
        authContextService.requireAdmin();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đề thi", null));

        if (req.options() == null || req.options().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "options không hợp lệ", null);
        }

        int correctIndex = deriveCorrectOptionIndex(req.options());

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(req.questionText());
        question.setQuestionNumber(req.questionNumber());
        question.setCorrectOptionIndex(correctIndex);
        question.setExplanation(req.explanation());
        question.setOptions(new ArrayList<>());

        List<Option> options = new ArrayList<>();
        for (ExamDtos.OptionDto opt : req.options()) {
            Option option = new Option();
            option.setQuestion(question);
            option.setOptionKey(parseOptionKey(opt.key()));
            option.setOptionText(opt.text());
            option.setCorrect(optIsCorrectAtIndex(option.getOptionKey(), correctIndex));
            options.add(option);
        }

        question.getOptions().clear();
        question.getOptions().addAll(options);

        Question saved = questionRepository.save(question);
        return toQuestionDto(saved);
    }

    public ExamDtos.QuestionDto updateQuestion(Integer questionId, ExamDtos.QuestionUpdateRequest req) {
        authContextService.requireAdmin();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy câu hỏi", null));

        if (req.questionText() != null) question.setQuestionText(req.questionText());
        question.setExplanation(req.explanation());

        if (req.options() != null && !req.options().isEmpty()) {
            int correctIndex = deriveCorrectOptionIndex(req.options());
            question.setCorrectOptionIndex(correctIndex);

            if (question.getOptions() == null) {
                question.setOptions(new ArrayList<>());
            }

            for (ExamDtos.OptionDto reqOpt : req.options()) {
                Option.OptionKey key = parseOptionKey(reqOpt.key());
                boolean isCorrect = optIsCorrectAtIndex(key, correctIndex);

                Optional<Option> existingOpt = question.getOptions().stream()
                        .filter(o -> o.getOptionKey() == key)
                        .findFirst();

                if (existingOpt.isPresent()) {
                    Option optToUpdate = existingOpt.get();
                    optToUpdate.setOptionText(reqOpt.text());
                    optToUpdate.setCorrect(isCorrect);
                } else {
                    Option newOption = new Option();
                    newOption.setQuestion(question);
                    newOption.setOptionKey(key);
                    newOption.setOptionText(reqOpt.text());
                    newOption.setCorrect(isCorrect);
                    question.getOptions().add(newOption);
                }
            }
        }

        Question saved = questionRepository.save(question);
        return toQuestionDto(saved);
    }

    public void deleteQuestion(Integer questionId) {
        authContextService.requireAdmin();
        questionRepository.deleteById(questionId);
    }

    private ExamDtos.ExamDto toExamDto(Exam exam) {
        return new ExamDtos.ExamDto(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getSubject(),
                exam.getSubjectColor(),
                exam.getTotalQuestions(),
                exam.getDurationMinutes(),
                exam.getDifficulty() != null ? exam.getDifficulty().name() : null,
                exam.getPassScore(),
                exam.getStatus() != null ? exam.getStatus().name() : null,
                exam.getAttemptCount(),
                exam.getAverageScore(),
                exam.getBannerColor(),
                exam.getCreatedByAdminId()
        );
    }

    private ExamDtos.QuestionDto toQuestionDto(Question question) {
        List<ExamDtos.OptionDto> optionDtos = new ArrayList<>();
        if (question.getOptions() != null) {
            optionDtos = question.getOptions().stream()
                    .sorted(Comparator.comparing(o -> o.getOptionKey().ordinal()))
                    .map(o -> new ExamDtos.OptionDto(o.getOptionKey().name(), o.getOptionText(), o.isCorrect()))
                    .toList();
        }

        return new ExamDtos.QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionNumber(),
                question.getExplanation(),
                optionDtos
        );
    }

    private int deriveCorrectOptionIndex(List<ExamDtos.OptionDto> options) {
        List<Option.OptionKey> correctKeys = options.stream()
                .filter(ExamDtos.OptionDto::is_correct)
                .map(o -> parseOptionKey(o.key()))
                .toList();
        if (correctKeys.size() != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "options phải có đúng 1 đáp án is_correct=true", null);
        }
        return optionKeyToIndex(correctKeys.get(0));
    }

    private Option.OptionKey parseOptionKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "option key không hợp lệ", null);
        }
        String norm = key.trim().toUpperCase(Locale.ROOT);
        return switch (norm) {
            case "A" -> Option.OptionKey.A;
            case "B" -> Option.OptionKey.B;
            case "C" -> Option.OptionKey.C;
            case "D" -> Option.OptionKey.D;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "option key phải là A/B/C/D", null);
        };
    }

    private int optionKeyToIndex(Option.OptionKey key) {
        return switch (key) {
            case A -> 0;
            case B -> 1;
            case C -> 2;
            case D -> 3;
        };
    }

    private boolean optIsCorrectAtIndex(Option.OptionKey key, int correctIndex) {
        return optionKeyToIndex(key) == correctIndex;
    }

    private Exam.Difficulty parseDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "difficulty không hợp lệ", null);
        }
        String norm = difficulty.trim().toLowerCase(Locale.ROOT);
        return switch (norm) {
            case "easy" -> Exam.Difficulty.easy;
            case "medium" -> Exam.Difficulty.medium;
            case "hard" -> Exam.Difficulty.hard;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "difficulty phải là easy/medium/hard", null);
        };
    }

    private Exam.Status parseExamStatus(String status) {
        String norm = status.trim().toLowerCase(Locale.ROOT);
        return switch (norm) {
            case "published" -> Exam.Status.published;
            case "draft" -> Exam.Status.draft;
            case "archived" -> Exam.Status.archived;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "status không hợp lệ", null);
        };
    }
}