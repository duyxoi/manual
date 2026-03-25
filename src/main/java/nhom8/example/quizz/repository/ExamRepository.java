package nhom8.example.quizz.repository;

import nhom8.example.quizz.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Integer> {

    Page<Exam> findBySubjectIgnoreCase(String subject, Pageable pageable);

    Page<Exam> findByDifficulty(Exam.Difficulty difficulty, Pageable pageable);

    Page<Exam> findBySubjectIgnoreCaseAndDifficulty(String subject, Exam.Difficulty difficulty, Pageable pageable);

    List<Exam> findBySubjectIgnoreCase(String subject);
}

