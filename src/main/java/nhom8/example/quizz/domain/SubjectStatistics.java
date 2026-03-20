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
@Table(name = "subject_statistics",
        uniqueConstraints = @UniqueConstraint(name = "unique_user_subject", columnNames = {"user_id", "subject"}))
public class SubjectStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "total_exams", nullable = false)
    private Integer totalExams = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "highest_score", precision = 5, scale = 2)
    private BigDecimal highestScore;

    @Column(name = "last_exam_date")
    private LocalDate lastExamDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

