using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.Entity
{
    public class Account
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public Guid AccountId { get; private set; }

        [Required]
        public Guid UserId { get; private set; }

        [Required]
        [StringLength(50)]
        public string AccountNumber { get; private set; }

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal Balance { get; private set; }

        [Required]
        [StringLength(50)]
        public string AccountType { get; private set; }

        [Required]
        public DateTime CreatedAt { get; private set; }

        public Account(Guid userId, string accountNumber, decimal balance, string accountType)
        {
            AccountId = Guid.NewGuid();
            UserId = userId;
            AccountNumber = accountNumber;
            Balance = balance;
            AccountType = accountType;
            CreatedAt = DateTime.UtcNow;
        }

        public void UpdateBalance(decimal newBalance)
        {
            if (newBalance < 0)
                throw new ArgumentException("Balance cannot be negative.");
            Balance = newBalance;
        }

        private Account() { } // For EF Core
    }
}
