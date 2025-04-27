using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain.models
{
    public class User
    {
        private Guid guid;
        private string userName;
        private string hashed;

        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public Guid Id { get;  set; }

        [Required]
        [StringLength(100)]
        public string Username { get;  set; }

        [Required]
        [StringLength(100)]
        public string Email { get;  set; }

        [Required]
        public string PasswordHash { get;  set; }

        [Required]
        public DateTime CreatedAt { get;  set; }

        public User()
        {
        }
        public void Update(string name, string email)
        {
            Username = name;
            Email = email;
        }
        public User(Guid id, string name, string email, string passwordHash, DateTime createdAt)
        {
            Id = id;
            Username = name;
            Email = email;
            this.PasswordHash = passwordHash;
            CreatedAt = createdAt;
        }

        public User(string name, string email, string passwordHash)
        {
            Username = name;
            Email = email;
            this.PasswordHash = passwordHash;
        }

        public User(Guid guid, string userName, string email, string hashed)
        {
            this.guid = guid;
            this.userName = userName;
            Email = email;
            this.hashed = hashed;
        }

        public override bool Equals(object? obj)
        {
            return obj is User user &&
                   Id.Equals(user.Id) &&
                   Username == user.Username &&
                   Email == user.Email &&
                   PasswordHash == user.PasswordHash &&
                   CreatedAt == user.CreatedAt;
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(Id, Username, Email, PasswordHash, CreatedAt);
        }
    }
}
