package test.projectcv.repository;

import test.projectcv.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Additional queries can be defined here, if needed.
    User findByEmail(String email);
}