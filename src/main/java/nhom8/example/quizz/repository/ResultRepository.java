package nhom8.example.quizz.repository;

import nhom8.example.quizz.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    Page<Result> findByUser_Id(Integer userId, Pageable pageable);

    Page<Result> findByExam_Id(Integer examId, Pageable pageable);

    Optional<Result> findByIdAndUser_Id(Integer resultId, Integer userId);

    List<Result> findByUser_IdOrderByCompletedAtDesc(Integer userId);

    List<Result> findByUser_IdAndExam_SubjectIgnoreCaseOrderByCompletedAtDesc(Integer userId, String subject);
}

