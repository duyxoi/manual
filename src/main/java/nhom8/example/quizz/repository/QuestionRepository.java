package nhom8.example.quizz.repository;

import nhom8.example.quizz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByExam_IdOrderByQuestionNumberAsc(Integer examId);

    Optional<Question> findByExam_IdAndQuestionNumber(Integer examId, Integer questionNumber);
}

