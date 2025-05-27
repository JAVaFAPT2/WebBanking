namespace Domain.Configuration;

public class LoanServiceSettings
{
    public decimal MaxLoanAmount { get; set; }
    public decimal MinLoanAmount { get; set; }
    public int MaxTermMonths { get; set; }
    public int MinTermMonths { get; set; }
} 