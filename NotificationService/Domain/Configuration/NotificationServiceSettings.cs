namespace NotificationService.Domain.Configuration;

public class NotificationServiceSettings
{
    // General settings
    public int DefaultRetryAttempts { get; set; } = 3;
    public int DefaultRetryDelaySeconds { get; set; } = 5;

    // Email specific settings (can be moved to a nested class if many)
    public string? DefaultFromEmail { get; set; }
    public string? DefaultFromName { get; set; }

    // SMS specific settings
    public string? DefaultSmsSenderId { get; set; }

    // Push specific settings - might be more complex and provider-dependent
    // public string? DefaultPushApiKey { get; set; }

    // Placeholder for provider-specific configurations if needed
    // public Dictionary<string, ProviderSettings> Providers { get; set; } = new();
}

// Example of a nested class for provider-specific settings if you have many
// public class ProviderSettings
// {
//     public string ApiKey { get; set; }
//     public string ApiSecret { get; set; }
//     public string BaseUrl { get; set; }
//     // ... other provider specific settings
// } 