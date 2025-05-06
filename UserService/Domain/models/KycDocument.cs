using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.models
{
    [Table("KycDocuments")]
    public class KycDocument
    {
        [Key]
        public Guid Id { get; set; }
        [Required]
        public Guid UserId { get; set; }
        [Required]
        public string DocumentType { get; set; } // e.g., Passport, ID Card
        [Required]
        public string DocumentPath { get; set; } // Path to the uploaded document
        [Required]
        public DateTime UploadedAt { get; set; }

        public KycDocument(Guid userId, string documentType, string documentPath)
        {
            Id = Guid.NewGuid();
            UserId = userId;
            DocumentType = documentType;
            DocumentPath = documentPath;
            UploadedAt = DateTime.UtcNow;
        }
    }
}
