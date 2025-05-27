using Domain.Configuration;
using Domain.Interface;
using Domain.Models;
using Microsoft.Extensions.Options;
using System.Security.Cryptography;

namespace Infrastructure.Services;

public class CardNumberGenerator : ICardNumberGenerator
{
    private readonly CardServiceSettings _settings;
    private readonly Random _random;

    public CardNumberGenerator(IOptions<CardServiceSettings> settings)
    {
        _settings = settings.Value;
        _random = new Random();
    }

    public string GenerateCardNumber(CardType cardType)
    {
        string bin = cardType switch
        {
            CardType.Debit => _settings.BinRangeDebit,
            CardType.Credit => _settings.BinRangeCredit,
            CardType.Virtual => _settings.BinRangeVirtual,
            _ => throw new ArgumentException("Invalid card type", nameof(cardType))
        };

        // Generate 9 random digits for the account number
        var accountNumber = string.Empty;
        for (int i = 0; i < 9; i++)
        {
            accountNumber += _random.Next(10).ToString();
        }

        // Combine BIN and account number
        var cardNumber = bin + accountNumber;

        // Calculate and append Luhn check digit
        var checkDigit = CalculateLuhnCheckDigit(cardNumber);
        return cardNumber + checkDigit;
    }

    public string GenerateCVV()
    {
        using var rng = RandomNumberGenerator.Create();
        var cvvBytes = new byte[2];
        rng.GetBytes(cvvBytes);
        var cvvNumber = BitConverter.ToUInt16(cvvBytes, 0) % 1000;
        return cvvNumber.ToString("D3");
    }

    private static int CalculateLuhnCheckDigit(string number)
    {
        var digits = number.Select(c => c - '0').ToArray();
        var sum = 0;
        var isEven = true;

        for (var i = digits.Length - 1; i >= 0; i--)
        {
            if (isEven)
            {
                var doubled = digits[i] * 2;
                sum += doubled > 9 ? doubled - 9 : doubled;
            }
            else
            {
                sum += digits[i];
            }
            isEven = !isEven;
        }

        var checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }

    public bool ValidateCardNumber(string cardNumber)
    {
        if (string.IsNullOrWhiteSpace(cardNumber) || cardNumber.Length != 16)
            return false;

        var sum = 0;
        var isEven = false;

        // Start from rightmost digit
        for (var i = cardNumber.Length - 1; i >= 0; i--)
        {
            if (!char.IsDigit(cardNumber[i]))
                return false;

            var digit = cardNumber[i] - '0';

            if (isEven)
            {
                digit *= 2;
                if (digit > 9)
                    digit -= 9;
            }

            sum += digit;
            isEven = !isEven;
        }

        return sum % 10 == 0;
    }

    public bool ValidateCVV(string cvv)
    {
        return !string.IsNullOrWhiteSpace(cvv) && 
               cvv.Length == 3 && 
               cvv.All(char.IsDigit);
    }
} 