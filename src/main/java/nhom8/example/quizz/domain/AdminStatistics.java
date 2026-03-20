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
@Table(name = "admin_statistics", uniqueConstraints = @UniqueConstraint(name = "unique_date", columnNames = {"stat_date"}))
public class AdminStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_users")
    private Integer totalUsers;

    @Column(name = "active_users")
    private Integer activeUsers;

    @Column(name = "locked_users")
    private Integer lockedUsers;

    @Column(name = "new_registrations")
    private Integer newRegistrations;

    @Column(name = "total_exams")
    private Integer totalExams;

    @Column(name = "published_exams")
    private Integer publishedExams;

    @Column(name = "draft_exams")
    private Integer draftExams;

    @Column(name = "archived_exams")
    private Integer archivedExams;

    @Column(name = "total_attempts")
    private Integer totalAttempts;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "pass_rate_percent", precision = 5, scale = 2)
    private BigDecimal passRatePercent;

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

