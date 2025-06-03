namespace TransactionService.Infrastructure.Configuration;

public class KafkaSettings
{
    public string BootstrapServers { get; set; } = string.Empty;
    public string ConsumerGroupId { get; set; } = "transaction-service-group"; // Example consumer group ID
    // We can also list specific topics here if the mapping is static, 
    // or rely on the eventTypesToTopics map for dynamic subscription.
    // public List<string> TopicsToSubscribe { get; set; } = new List<string>();
    // Add other common Kafka settings if needed, e.g., security protocol, SASL mechanism, etc.
} 