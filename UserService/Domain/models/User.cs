using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace Domain.models
{
    public class User
    {

        [Key]
        public Guid Id { get; set; }
        [Required]
        public required string Name { get; set; }
        [Required]
        [Length(7, 20)]
        public required string Email { get; set; }
        [Required]
        [PasswordPropertyText]
        public required string PasswordHash { get; set; }
        public DateTime CreatedAt { get; set; }

        public User()
        {
        }
        public void Update(string name, string email)
        {
            Name = name;
            Email = email;
        }
        public User(Guid id, string name, string email, string passwordHash, DateTime createdAt)
        {
            Id = id;
            Name = name;
            Email = email;
            this.PasswordHash = passwordHash;
            CreatedAt = createdAt;
        }

        public User(string name, string email, string passwordHash)
        {
            Name = name;
            Email = email;
            this.PasswordHash = passwordHash;
        }

        public override bool Equals(object? obj)
        {
            return obj is User user &&
                   Id.Equals(user.Id) &&
                   Name == user.Name &&
                   Email == user.Email &&
                   PasswordHash == user.PasswordHash &&
                   CreatedAt == user.CreatedAt;
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(Id, Name, Email, PasswordHash, CreatedAt);
        }
    }
}
