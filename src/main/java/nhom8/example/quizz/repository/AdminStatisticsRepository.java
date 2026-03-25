package nhom8.example.quizz.repository;

import nhom8.example.quizz.entity.AdminStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdminStatisticsRepository extends JpaRepository<AdminStatistics, Integer> {

    List<AdminStatistics> findByStatDateBetween(LocalDate from, LocalDate to);
}

