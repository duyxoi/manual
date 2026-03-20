package nhom8.example.quizz.repository;

import nhom8.example.quizz.domain.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Integer> {

    List<DailyActivity> findByActivityDate(LocalDate activityDate);
}

