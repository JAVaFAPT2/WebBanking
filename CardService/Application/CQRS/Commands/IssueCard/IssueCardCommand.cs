using Domain.Interface;
using Domain.Models;
using Domain.ValueObjects;
using FluentValidation;
using MediatR;

namespace Application.CQRS.Commands.IssueCard;

public record IssueCardCommand(
    Guid AccountId,
    string CardholderName,
    CardType Type,
    Money CreditLimit) : IRequest<Guid>;

public class IssueCardCommandHandler : IRequestHandler<IssueCardCommand, Guid>
{
    private readonly ICardRepository _cardRepository;
    private readonly ICardNumberGenerator _cardNumberGenerator;

    public IssueCardCommandHandler(
        ICardRepository cardRepository,
        ICardNumberGenerator cardNumberGenerator)
    {
        _cardRepository = cardRepository;
        _cardNumberGenerator = cardNumberGenerator;
    }

    public async Task<Guid> Handle(IssueCardCommand request, CancellationToken cancellationToken)
    {
        var cardNumber = _cardNumberGenerator.GenerateCardNumber(request.Type);
        var cvv = _cardNumberGenerator.GenerateCVV();

        var card = Card.Issue(
            request.AccountId,
            request.CardholderName,
            request.Type,
            cardNumber,
            cvv,
            request.CreditLimit
        );

        await _cardRepository.AddAsync(card);
        return card.Id;
    }
}

public class IssueCardCommandValidator : AbstractValidator<IssueCardCommand>
{
    public IssueCardCommandValidator()
    {
        RuleFor(x => x.AccountId)
            .NotEmpty()
            .WithMessage("Account ID is required");

        RuleFor(x => x.CardholderName)
            .NotEmpty()
            .MaximumLength(100)
            .WithMessage("Cardholder name is required and must not exceed 100 characters");

        RuleFor(x => x.Type)
            .IsInEnum()
            .WithMessage("Invalid card type");

        RuleFor(x => x.CreditLimit.Amount)
            .GreaterThanOrEqualTo(0)
            .When(x => x.Type == CardType.Credit)
            .WithMessage("Credit limit must be non-negative for credit cards");

        RuleFor(x => x.CreditLimit.Currency)
            .NotEmpty()
            .Length(3)
            .When(x => x.Type == CardType.Credit)
            .WithMessage("Valid currency code is required for credit cards");
    }
} 