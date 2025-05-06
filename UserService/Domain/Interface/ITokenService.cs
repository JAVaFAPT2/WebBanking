namespace Domain.Interface
{
    public interface ITokenService
    {
        string GenerateToken(Guid userId, string username, string email);
    }
}
