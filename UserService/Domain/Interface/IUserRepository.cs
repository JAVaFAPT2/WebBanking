using Domain.models;

namespace Domain.Interface
{
    public interface IUserRepository
    {
        Task<User?> GetByIdAsync(Guid id);
        Task<User?> GetByEmailAsync(string email);
        Task<IEnumerable<User?>> GetAllAsync();
        Task<bool> ExistsByUsernameAsync(string username);
        Task<bool> ExistsByEmailAsync(string email);
        Task<Guid> AddAsync(User? user);
        Task UpdateAsync(User? user);
        Task DeleteAsync(Guid id);
    }
}
