using Domain.Interface;

namespace Infrastructure.Persistence.Service
{
    public class PasswordHasher : IPasswordHasher
    {
        public string Hash(string password)
        {
            if (string.IsNullOrWhiteSpace(password))
                throw new ArgumentNullException(nameof(password));

            return BCrypt.Net.BCrypt.HashPassword(password);
        }

        public bool Verify(string password, string hashedPassword)
        {
            if (string.IsNullOrWhiteSpace(password) || string.IsNullOrWhiteSpace(hashedPassword))
                throw new ArgumentNullException("Password or hashed password cannot be null or empty.");

            return BCrypt.Net.BCrypt.Verify(password, hashedPassword);
        }
    }
}