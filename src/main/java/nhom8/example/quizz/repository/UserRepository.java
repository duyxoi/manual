package nhom8.example.quizz.repository;

import nhom8.example.quizz.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<AppUser> findByRole(AppUser.Role role, Pageable pageable);

    Page<AppUser> findByStatus(AppUser.Status status, Pageable pageable);

    Page<AppUser> findByRoleAndStatus(AppUser.Role role, AppUser.Status status, Pageable pageable);
}

