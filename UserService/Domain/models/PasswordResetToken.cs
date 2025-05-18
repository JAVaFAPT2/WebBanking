using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Domain.models;

namespace Domain.Models
{
    public class PasswordResetToken
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public Guid UserId { get; set; }

        [Required]
        [StringLength(256)]
        public string Token { get; set; }

        [Required]
        public DateTime Expiry { get; set; }

        [Required]
        public bool IsUsed { get; set; } = false;

        // Navigation property (optional)
        [ForeignKey("UserId")]
        public User User { get; set; }
    }
}