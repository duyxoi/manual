package nhom8.example.quizz.service;

import nhom8.example.quizz.dto.QuestionDtos;
import nhom8.example.quizz.entity.Question;
import nhom8.example.quizz.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionRepository questionRepository;

    public List<QuestionDtos.QuestionDto> findByExam(Integer examId) {
        // 1. Lấy danh sách entity từ database
        List<Question> questions = questionRepository.findByExam_IdOrderByQuestionNumberAsc(examId);

        // 2. Dùng Stream để map List<Question> thành List<QuestionDto>
        return questions.stream().map(question -> {
            var options = question.getOptions(); // Giả sử đây là List chứa các đáp án
            int size = options != null ? options.size() : 0;

            // Trích xuất dữ liệu an toàn, kiểm tra size để tránh lỗi IndexOutOfBounds
            return new QuestionDtos.QuestionDto(
                    question.getId(),
                    question.getQuestionText(),
                    size > 0 ? options.get(0).getOptionText() : null,
                    size > 1 ? options.get(1).getOptionText() : null,
                    size > 2 ? options.get(2).getOptionText() : null,
                    size > 3 ? options.get(3).getOptionText() : null
            );
        }).toList(); // Gom lại thành List và trả về (Hỗ trợ từ Java 16+)
    }
}
