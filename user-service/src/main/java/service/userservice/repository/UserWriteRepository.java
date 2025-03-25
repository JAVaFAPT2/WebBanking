package service.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.userservice.command.internal.models.UserWriteModel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWriteRepository extends JpaRepository<UserWriteModel, UUID> {
    Optional<UserWriteModel> findByUsername(String username);

    Optional<UserWriteModel> findByEmail(String email);
}
