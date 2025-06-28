using Domain.models;
using Domain.Interface;
using Infrastructure.Persistence.DBContext;
using Microsoft.EntityFrameworkCore;
using Polly;
using Domain.Models;

namespace Infrastructure.Persistence.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly ApplicationDbContext _context;

        public UserRepository(ApplicationDbContext context)
        {
            _context = context;
        }

        public async Task<Guid> AddAsync(User? user)
        {
            if (user == null)
            {
                throw new ArgumentNullException(nameof(user), "User cannot be null.");
            }

            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(async () =>
            {
                await _context.Users.AddAsync(user);
                await _context.SaveChangesAsync();
                return user.Id;
            });
        }


        public async Task UpdateAsync(User? user)
        {
            if (user == null)
            {
                throw new ArgumentNullException(nameof(user), "User cannot be null.");
            }

            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            await policy.ExecuteAsync(async () =>
            {
                _context.Users.Update(user);
                await _context.SaveChangesAsync();
            });
        }


        public async Task DeleteAsync(Guid id)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            await policy.ExecuteAsync(async () =>
            {
                var user = await GetByIdAsync(id);
                if (user != null)
                {
                    _context.Users.Remove(user);
                    await _context.SaveChangesAsync();
                }
            });
        }

        public async Task<User?> GetByIdAsync(Guid id)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.FindAsync(id).AsTask());
        }

        public async Task<User?> GetByUsernameAsync(string username)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.FirstOrDefaultAsync(u => u != null && u.Username == username));
        }

        public async Task<User?> GetByEmailAsync(string email)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.FirstOrDefaultAsync(u => u != null && u.Email == email));
        }

        public async Task<IEnumerable<User?>> GetAllAsync()
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.ToListAsync());
        }

        public async Task<bool> ExistsByUsernameAsync(string username)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.AnyAsync(u => u != null && u.Username == username));
        }

        public async Task<bool> ExistsByEmailAsync(string email)
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.Users.AnyAsync(u => u != null && u.Email == email));
        }

        public async Task<int> SaveChangesAsync()
        {
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            return await policy.ExecuteAsync(() => _context.SaveChangesAsync());
        }

        public async Task<string> GeneratePasswordResetTokenAsync(User user)
        {
            if (user == null) throw new ArgumentNullException(nameof(user));
            var token = Guid.NewGuid().ToString();
            if (user.PasswordResetTokens == null)
                user.PasswordResetTokens = new List<PasswordResetToken>();
            user.PasswordResetTokens.Add(new PasswordResetToken
            {
                Token = token,
                User = user,
                Expiry = DateTime.UtcNow.AddHours(1),
                IsUsed = false
            });
            await _context.SaveChangesAsync();
            return token;
        }
    }
}
