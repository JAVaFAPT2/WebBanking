namespace Domain.Interface
{
    public interface ITokenService
    {
        Task<string> GeneratePasswordResetTokenAsync(Guid userId);
        Task<bool> ValidatePasswordResetTokenAsync(Guid userId, string token);
        Task InvalidatePasswordResetTokenAsync(Guid userId, string token);
        string GenerateToken(Guid userId, string username, string email);
    }
}
