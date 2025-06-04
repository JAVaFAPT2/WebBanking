namespace Presentation.Dto;

public class KycVerificationDto
{
    public string Status { get; set; } // "Approved" or "Rejected"
    public string VerifierNotes { get; set; }
}