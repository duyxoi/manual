package nhom8.example.quizz.repository;

import nhom8.example.quizz.domain.SubjectStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectStatisticsRepository extends JpaRepository<SubjectStatistics, Integer> {

    List<SubjectStatistics> findByUser_Id(Integer userId);

    Optional<SubjectStatistics> findByUser_IdAndSubject(Integer userId, String subject);
}

