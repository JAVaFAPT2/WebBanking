namespace FundTransferService.Domain.Configuration
{
    public class KafkaSettings
    {
        public string BootstrapServers { get; set; } = "localhost:9092";
        public string ProducerTopic { get; set; } = "fundtransfer-events"; // Example topic for outgoing messages
        public string ConsumerGroupId { get; set; } = "fundtransfer-service-group";
        public string ConsumerTopic { get; set; } = "account-events"; // Example topic for incoming messages
        public bool EnableAutoCommit { get; set; } = false; // For consumer, usually better to commit manually
        public string AutoOffsetReset { get; set; } = "Earliest"; // For consumer: "Earliest" or "Latest"

        // Producer specific settings
        public string Acks { get; set; } = "All"; // Leader and all ISRs must acknowledge
        public int MessageSendMaxRetries { get; set; } = 3;
        public int RetryBackoffMs { get; set; } = 1000;

        // SSL/SASL settings (if needed)
        // public string SecurityProtocol { get; set; }
        // public string SaslMechanism { get; set; }
        // public string SaslUsername { get; set; }
        // public string SaslPassword { get; set; }
        // public string SslCaLocation { get; set; }
        // public string SslCertificateLocation { get; set; }
        // public string SslKeyLocation { get; set; }
    }
} 