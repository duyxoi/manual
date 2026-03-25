package nhom8.example.quizz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_statistics")
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "total_exams_taken")
    private Integer totalExamsTaken = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "highest_score", precision = 5, scale = 2)
    private BigDecimal highestScore;

    @Column(name = "total_correct")
    private Integer totalCorrect = 0;

    @Column(name = "total_wrong")
    private Integer totalWrong = 0;

    @Column(name = "total_time_spent_hours")
    private Integer totalTimeSpentHours = 0;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @Column(name = "last_exam_date")
    private LocalDate lastExamDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

