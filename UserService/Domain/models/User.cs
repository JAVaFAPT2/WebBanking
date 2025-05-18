using Domain.Models;
using Domain.ValueObjects;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain.models
{
    [Table("Users")]
    public class User
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public Guid Id { get; set; }

        [Required]
        [StringLength(50, MinimumLength = 3)]
        [Column(TypeName = "nvarchar(50)")]
        public string Username { get; set; }

        [Required]
        [StringLength(50)]
        [Column(TypeName = "nvarchar(50)")]
        public string FirstName { get; set; }

        [Required]
        [StringLength(50)]
        [Column(TypeName = "nvarchar(50)")]
        public string LastName { get; set; }

        [Required]
        [EmailAddress]
        [StringLength(100)]
        [Column(TypeName = "nvarchar(100)")]
        public string Email { get; set; }

        [Required]
        [Phone]
        [StringLength(20)]
        [Column(TypeName = "nvarchar(20)")]
        public string PhoneNumber { get; set; }

        [Required]
        [StringLength(128)]
        [Column(TypeName = "nvarchar(128)")]
        public string PasswordHash { get; set; }

        [Required]
        [Column(TypeName = "date")]
        public DateTime DateOfBirth { get; set; }

        [Required]
        public Address Address { get; set; }

        public KycDocument Document { get; set; }

        [Required]
        [Column(TypeName = "datetime2")]
        public DateTime CreatedAt { get; set; }

        [Column(TypeName = "datetime2")]
        public DateTime? UpdatedAt { get; set; }

        [Required]
        [DefaultValue(true)]
        public bool IsActive { get; set; }

        public KycStatus KycStatus { get; set; } = KycStatus.Unverified;
        public DateTime? KycVerificationDate { get; set; }

        [StringLength(100)]
        public string KycVerificationNotes { get; set; }

        public ICollection<PasswordResetToken> PasswordResetTokens { get; set; }




        /// <summary>
        /// Private constructor for ORM
        /// </summary>
        private User() { }

        /// <summary>
        /// Creates a new user with the specified details
        /// </summary>
        /// <param name="username">Unique username for the user</param>
        /// <param name="firstName">User's first name</param>
        /// <param name="lastName">User's last name</param>
        /// <param name="email">User's email address</param>
        /// <param name="phoneNumber">User's phone number</param>
        /// <param name="passwordHash">Hashed password</param>
        /// <param name="dateOfBirth">User's date of birth</param>
        /// <param name="address">User's address</param>
        public User(string username, string firstName, string lastName, string email,
                  string phoneNumber, string passwordHash, DateTime dateOfBirth, Address address)
        {
            if (string.IsNullOrWhiteSpace(username))
                throw new ArgumentNullException(nameof(username));
            if (string.IsNullOrWhiteSpace(firstName))
                throw new ArgumentNullException(nameof(firstName));
            if (string.IsNullOrWhiteSpace(lastName))
                throw new ArgumentNullException(nameof(lastName));
            if (string.IsNullOrWhiteSpace(email))
                throw new ArgumentNullException(nameof(email));
            if (string.IsNullOrWhiteSpace(phoneNumber))
                throw new ArgumentNullException(nameof(phoneNumber));
            if (string.IsNullOrWhiteSpace(passwordHash))
                throw new ArgumentNullException(nameof(passwordHash));
            if (address == null)
                throw new ArgumentNullException(nameof(address));

            Id = Guid.NewGuid();
            Username = username;
            FirstName = firstName;
            LastName = lastName;
            Email = email;
            PhoneNumber = phoneNumber;
            PasswordHash = passwordHash;
            DateOfBirth = dateOfBirth;
            Address = address;
            KycStatus = KycStatus.Pending;
            CreatedAt = DateTime.UtcNow;
            IsActive = true;
        }

        /// <summary>
        /// Updates the user's profile information
        /// </summary>
        public void UpdateProfile(string firstName, string lastName, string phoneNumber, Address address)
        {
            if (string.IsNullOrWhiteSpace(firstName))
                throw new ArgumentNullException(nameof(firstName));
            if (string.IsNullOrWhiteSpace(lastName))
                throw new ArgumentNullException(nameof(lastName));
            if (string.IsNullOrWhiteSpace(phoneNumber))
                throw new ArgumentNullException(nameof(phoneNumber));
            if (address == null)
                throw new ArgumentNullException(nameof(address));

            FirstName = firstName;
            LastName = lastName;
            PhoneNumber = phoneNumber;
            Address = address;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Updates the user's password
        /// </summary>
        public void UpdatePassword(string newPasswordHash)
        {
            if (string.IsNullOrWhiteSpace(newPasswordHash))
                throw new ArgumentNullException(nameof(newPasswordHash));

            PasswordHash = newPasswordHash;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Updates the user's KYC status
        /// </summary>
        public void UpdateKycStatus(KycStatus status)
        {
            KycStatus = status;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Deactivates the user account
        /// </summary>
        public void Deactivate()
        {
            IsActive = false;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Reactivates a previously deactivated user account
        /// </summary>
        public void Activate()
        {
            IsActive = true;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Gets the full name of the user
        /// </summary>
        [NotMapped]
        public string FullName => $"{FirstName} {LastName}";

        /// <summary>
        /// Checks if the user is of legal age (18+)
        /// </summary>
        [NotMapped]
        public bool IsLegalAge => DateTime.Today.AddYears(-18) >= DateOfBirth;
    }
}
