package service.orchestrationservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import service.orchestrationservice.event.OrchestrationEvent;
import service.orchestrationservice.model.TransferRequest;
import service.orchestrationservice.workflow.SagaOrchestrator;

import static java.util.UUID.randomUUID;

@RestController
@RequestMapping("/orchestration")
public class OrchestrationController {

    private final SagaOrchestrator sagaOrchestrator;

    public OrchestrationController(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @PostMapping("/transfers")
    public Mono<OrchestrationEvent> initiateTransfer(@RequestBody TransferRequest transferRequest) {
        OrchestrationEvent event = new OrchestrationEvent(
                randomUUID().toString(),
                transferRequest.getFromAccountId(),
                transferRequest.getToAccountId(),
                transferRequest.getAmount()
        );

        return sagaOrchestrator.orchestrateTransfer(event);
    }
}
