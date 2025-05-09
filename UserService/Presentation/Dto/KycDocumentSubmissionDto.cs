namespace Presentation.Dto;

// DTOs for KYC operations
public class KycDocumentSubmissionDto
{
    public string DocumentType { get; set; }
    public string DocumentNumber { get; set; }
    public string IssuingCountry { get; set; }
    public DateTime? ExpiryDate { get; set; }
    public IFormFile DocumentFile { get; set; }
}