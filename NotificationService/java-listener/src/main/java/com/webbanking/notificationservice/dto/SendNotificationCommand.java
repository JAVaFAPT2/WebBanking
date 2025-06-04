package com.webbanking.notificationservice.dto;

import java.util.Map;

/**
 * Command object for requesting notification service to send a notification
 */
public record SendNotificationCommand(
        String sagaId,
        String transactionId, // ID of the overall fund transfer transaction
        String userId, // The user to notify
        String recipientAddress, // e.g., email address, phone number, device token
        String messageType, // e.g., "EMAIL", "SMS", "PUSH"
        String subject, // Title for email/push
        String body, // Content of the notification
        Map<String, String> templateModel // For personalized notifications
) {
} 