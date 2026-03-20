package nhom8.example.quizz.repository;

import nhom8.example.quizz.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    Optional<Answer> findByResult_IdAndQuestion_Id(Integer resultId, Integer questionId);

    List<Answer> findByResult_Id(Integer resultId);

    long countByResult_Id(Integer resultId);

    long countByResult_IdAndMarkedForReviewTrue(Integer resultId);
}

