using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain.models
{
    public class KycDocument
    {
        [Key]
        public Guid Id { get; set; }

        public Guid UserId { get; set; }

        [Required]
        [StringLength(50)]
        public string DocumentType { get; set; } // Passport, ID Card, Driver's License, etc.

        [Required]
        [StringLength(100)]
        public string DocumentNumber { get; set; }

        [StringLength(50)]
        public string IssuingCountry { get; set; }

        public DateTime? ExpiryDate { get; set; }

        public DateTime SubmissionDate { get; set; }

        [StringLength(255)]
        public string DocumentPath { get; set; } // Path to the stored document

        [Required]
        [StringLength(20)]
        public string Status { get; set; } // Pending, Approved, Rejected

        public DateTime? VerificationDate { get; set; }

        public string VerifierNotes { get; set; }

        [ForeignKey("UserId")]
        public virtual User User { get; set; }
    }
}