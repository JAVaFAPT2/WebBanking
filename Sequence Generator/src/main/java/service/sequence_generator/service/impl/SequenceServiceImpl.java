package service.sequence_generator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import service.sequence_generator.models.Sequence;
import service.sequence_generator.service.SequenceService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {

    private final service.sequence_generator.repository.SequenceRepository sequenceRepository;
    @Override
    public Sequence createSequence() {
        log.info("creating a account number");
        return sequenceRepository.findById(1L)
                .map(sequence -> {
                    sequence.setAcccountNumber(sequence.getAcccountNumber() + 1);
                    return sequenceRepository.save(sequence);
                }).orElseGet(() -> sequenceRepository.save(Sequence.builder().acccountNumber(1L).build()));
    }
}
