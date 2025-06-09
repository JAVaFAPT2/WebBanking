namespace Application.CQRS.DTO
{
    public class AuthResponse
    {
        public string? Token { get; set; }
        public string? Username { get; set; }
        public string? Email { get; set; }
        public Guid UserId { get; set; }
    }
} 