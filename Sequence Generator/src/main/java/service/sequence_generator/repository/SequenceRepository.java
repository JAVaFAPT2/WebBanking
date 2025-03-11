package service.sequence_generator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import service.sequence_generator.models.Sequence;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    @Query("SELECT COUNT(s) FROM Sequence s")
    int countAll();
    Sequence findFirstByOrderByIdDesc();
}
