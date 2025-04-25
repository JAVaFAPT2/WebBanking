package service.messagebroker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.messagebroker.DTo.FraudAlertEventDTO;
import service.messagebroker.DTo.NotificationEventDTO;
import service.messagebroker.DTo.TransactionEventDTO;
import service.messagebroker.DTo.UserActivityEventDTO;
import service.messagebroker.producer.MessageProducer;

@RestController
@RequestMapping("/api")
public class KafkaController {

    @Autowired
    private MessageProducer messageProducer;

    @PostMapping("/transactions")
    public ResponseEntity<String> sendTransactionEvent(@RequestBody TransactionEventDTO dto) {
        messageProducer.sendTransactionEvent(
                dto.accountId(),
                dto.transactionId(),
                dto.amount(),
                dto.description(),
                dto.isInternational()
        );
        return ResponseEntity.ok("Transaction event sent");
    }

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotificationEvent(@RequestBody NotificationEventDTO dto) {
        messageProducer.sendNotificationEvent(
                dto.UserId(),
                dto.Title(),
                dto.Content(),
                dto.Notification(),
                dto.priority()
        );
        return ResponseEntity.ok("Notification event sent");
    }

    @PostMapping("/user-activities")
    public ResponseEntity<String> sendUserActivityEvent(@RequestBody UserActivityEventDTO dto) {
        messageProducer.sendUserActivityEvent(
                dto.userId(),
                dto.activity(),
                dto.userActivity(),
                dto.details()
        );
        return ResponseEntity.ok("User activity event sent");
    }

    @PostMapping("/fraud-alerts")
    public ResponseEntity<String> sendFraudAlertEvent(@RequestBody FraudAlertEventDTO dto) {
        messageProducer.sendFraudAlertEvent(
                dto.accountId(),
                dto.transactionId(),
                dto.fraudType(),
                dto.details()
        );
        return ResponseEntity.ok("Fraud alert event sent");
    }
}