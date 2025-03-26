package service.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import service.userservice.query.internal.models.UserReadModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserReadRepository extends JpaRepository<UserReadModel, UUID> {

    Optional<UserReadModel> findByUsername(String username);

    Optional<UserReadModel> findByEmail(String email);

    List<UserReadModel> findByRolesContaining(String role);

    @Query("SELECT u FROM UserReadModel u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserReadModel> searchUsers(@Param("searchTerm") String searchTerm);

    long countByRolesContaining(String role);
}
