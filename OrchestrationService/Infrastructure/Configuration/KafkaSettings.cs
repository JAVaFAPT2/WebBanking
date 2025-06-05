namespace OrchestrationService.Infrastructure.Configuration;

/// <summary>
/// Settings for Kafka message broker
/// </summary>
public class KafkaSettings
{
    /// <summary>
    /// Connection string for Kafka bootstrap servers (comma-separated list)
    /// </summary>
    public string BootstrapServers { get; set; } = "kafka:9092";
    
    /// <summary>
    /// Client ID for Kafka producer
    /// </summary>
    public string ClientId { get; set; } = "orchestration-service";
    
    /// <summary>
    /// Consumer group ID for Kafka consumer
    /// </summary>
    public string GroupId { get; set; } = "orchestration-service-group";
    
    /// <summary>
    /// Topic for account service commands
    /// </summary>
    public string AccountCommandTopic { get; set; } = "account-commands";
    
    /// <summary>
    /// Topic for account service events/replies
    /// </summary>
    public string AccountEventTopic { get; set; } = "account-events";
    
    /// <summary>
    /// Topic for notification service commands
    /// </summary>
    public string NotificationCommandTopic { get; set; } = "notification-commands";
    
    /// <summary>
    /// Topic for notification service events/replies
    /// </summary>
    public string NotificationEventTopic { get; set; } = "notification-events";
    
    /// <summary>
    /// Topic for card service commands
    /// </summary>
    public string CardCommandTopic { get; set; } = "card-commands";
    
    /// <summary>
    /// Topic for card service events/replies
    /// </summary>
    public string CardEventTopic { get; set; } = "card-events";
    
    /// <summary>
    /// Topic for loan service commands
    /// </summary>
    public string LoanCommandTopic { get; set; } = "loan-commands";
    
    /// <summary>
    /// Topic for loan service events/replies
    /// </summary>
    public string LoanEventTopic { get; set; } = "loan-events";
} 