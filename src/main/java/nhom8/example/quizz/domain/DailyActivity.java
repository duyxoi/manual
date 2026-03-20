package nhom8.example.quizz.domain;

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
@Table(name = "daily_activity",
        uniqueConstraints = @UniqueConstraint(name = "unique_date_hour", columnNames = {"activity_date", "hour_of_day"}))
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "hour_of_day")
    private Integer hourOfDay;

    @Column(name = "exam_attempts", nullable = false)
    private Integer examAttempts = 0;

    @Column(name = "active_users", nullable = false)
    private Integer activeUsers = 0;

    @Column(name = "total_score_sum", precision = 10, scale = 2)
    private BigDecimal totalScoreSum;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

