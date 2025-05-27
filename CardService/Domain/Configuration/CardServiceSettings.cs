namespace Domain.Configuration;

public class CardServiceSettings
{
    public string CacheKeyPrefix { get; set; } = "card:";
    public int CacheExpirationMinutes { get; set; } = 30;
    public string BinRangeDebit { get; set; } = "4532";
    public string BinRangeCredit { get; set; } = "4716";
    public string BinRangeVirtual { get; set; } = "4916";
} 