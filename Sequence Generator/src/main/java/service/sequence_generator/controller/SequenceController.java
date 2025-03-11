package service.sequence_generator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.sequence_generator.models.Sequence;
import service.sequence_generator.service.SequenceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sequence")
public class SequenceController {
    private final SequenceService sequenceService;

    @PostMapping
    public Sequence createSequence() {
        return sequenceService.createSequence();
    }
}


