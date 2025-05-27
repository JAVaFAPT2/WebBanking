using Domain.Models;

namespace Domain.Interface;

public interface ICardRepository
{
    Task<Card?> GetByIdAsync(Guid id);
    Task<IEnumerable<Card>> GetByAccountIdAsync(Guid accountId);
    Task<Card> AddAsync(Card card);
    Task UpdateAsync(Card card);
    Task DeleteAsync(Card card);
    Task<bool> ExistsAsync(Guid id);
} 