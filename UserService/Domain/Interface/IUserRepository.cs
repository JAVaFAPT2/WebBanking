using Domain.models;

namespace Domain.Interface
{
    public interface IUserRepository
    {
        Task<Domain.models.User?> GetByIdAsync(Guid id);
        Task<Domain.models.User?> GetByEmailAsync(string email);
        Task<IEnumerable<Domain.models.User?>> GetAllAsync();
        Task<bool> ExistsByUsernameAsync(string username);
        Task<bool> ExistsByEmailAsync(string email);
        Task<Domain.models.User?> GetByUsernameAsync(string UserName);
        Task<Guid> AddAsync(Domain.models.User? user);
        Task UpdateAsync(Domain.models.User? user);
        Task DeleteAsync(Guid id);
        Task<string> GeneratePasswordResetTokenAsync(Domain.models.User user);
    }
}
