using System;
using System.Collections.Concurrent;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;
using Domain.Interface;
using Microsoft.IdentityModel.Tokens;

namespace Infrastructure.Persistence.Service
{
    public class TokenService : ITokenService
    {
        private readonly string _secretKey;
        // In-memory store for demo; replace with persistent storage in production
        private static readonly ConcurrentDictionary<Guid, (string Token, DateTime Expiry)> _resetTokens = new();

        public TokenService(string secretKey)
        {
            if (string.IsNullOrWhiteSpace(secretKey))
                throw new ArgumentNullException(nameof(secretKey));

            _secretKey = secretKey;
        }

        public string GenerateToken(Guid userId, string username, string email)
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(_secretKey);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, userId.ToString()),
                    new Claim(ClaimTypes.Name, username),
                    new Claim(ClaimTypes.Email, email)
                }),
                Expires = DateTime.UtcNow.AddHours(1),
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }

        // --- Password Reset Token Methods ---

        public Task<string> GeneratePasswordResetTokenAsync(Guid userId)
        {
            // Generate a secure random token
            var token = Convert.ToBase64String(Guid.NewGuid().ToByteArray());
            var expiry = DateTime.UtcNow.AddHours(1);

            _resetTokens[userId] = (token, expiry);

            return Task.FromResult(token);
        }

        public Task<bool> ValidatePasswordResetTokenAsync(Guid userId, string token)
        {
            if (_resetTokens.TryGetValue(userId, out var entry))
            {
                if (entry.Token == token && entry.Expiry > DateTime.UtcNow)
                {
                    return Task.FromResult(true);
                }
            }
            return Task.FromResult(false);
        }

        public Task InvalidatePasswordResetTokenAsync(Guid userId, string token)
        {
            _resetTokens.TryRemove(userId, out _);
            return Task.CompletedTask;
        }
    }
}
