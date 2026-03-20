package nhom8.example.quizz.repository;

import nhom8.example.quizz.domain.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Integer> {

    Optional<UserStatistics> findByUser_Id(Integer userId);
}

