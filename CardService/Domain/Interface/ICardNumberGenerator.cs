using Domain.Models;

namespace Domain.Interface;

public interface ICardNumberGenerator
{
    string GenerateCardNumber(CardType cardType);
    string GenerateCVV();
} 