using Application.CQRS.Commands.IssueCard;
using CardService.Protos;
using Domain.Interface;
using Domain.Models;
using DomainMoney = Domain.ValueObjects.Money;
using Grpc.Core;
using MediatR;
using Microsoft.Extensions.Logging;

namespace CardService.Services;

public class CardGrpcService : Protos.CardService.CardServiceBase
{
    private readonly IMediator _mediator;
    private readonly ICardRepository _cardRepository;
    private readonly ICardNumberGenerator _cardNumberGenerator;
    private readonly ILogger<CardGrpcService> _logger;

    public CardGrpcService(
        IMediator mediator,
        ICardRepository cardRepository,
        ICardNumberGenerator cardNumberGenerator,
        ILogger<CardGrpcService> logger)
    {
        _mediator = mediator;
        _cardRepository = cardRepository;
        _cardNumberGenerator = cardNumberGenerator;
        _logger = logger;
    }

    public override async Task<IssueCardResponse> IssueCard(IssueCardRequest request, ServerCallContext context)
    {
        try
        {
            var cardType = request.CardType.ToUpper() switch
            {
                "DEBIT" => CardType.Debit,
                "CREDIT" => CardType.Credit,
                "VIRTUAL" => CardType.Virtual,
                _ => throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid card type"))
            };

            var creditLimit = new DomainMoney(request.CreditLimit.Amount / 100m, request.CreditLimit.Currency);
            var command = new IssueCardCommand(
                Guid.Parse(request.AccountId),
                "Cardholder Name", // TODO: Get from account service
                cardType,
                creditLimit
            );

            var cardId = await _mediator.Send(command);
            var card = await _cardRepository.GetByIdAsync(cardId);

            if (card == null)
                throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve issued card"));

            return new IssueCardResponse
            {
                CardId = card.Id.ToString(),
                CardNumber = MaskCardNumber(card.CardNumber),
                ExpiryDate = card.ExpiryDate.ToString("MM/yy")
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error issuing card");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to issue card"));
        }
    }

    public override async Task<CardResponse> ActivateCard(ActivateCardRequest request, ServerCallContext context)
    {
        try
        {
            var card = await _cardRepository.GetByIdAsync(Guid.Parse(request.CardId));
            if (card == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Card not found"));

            // TODO: Validate activation code
            card.Activate();
            await _cardRepository.UpdateAsync(card);

            return MapToCardResponse(card);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error activating card");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to activate card"));
        }
    }

    public override async Task<CardResponse> BlockCard(BlockCardRequest request, ServerCallContext context)
    {
        try
        {
            var card = await _cardRepository.GetByIdAsync(Guid.Parse(request.CardId));
            if (card == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Card not found"));

            if (request.Block)
            {
                card.Block(request.Reason);
            }
            else
            {
                throw new RpcException(new Status(StatusCode.Unimplemented, "Unblocking cards is not supported"));
            }

            await _cardRepository.UpdateAsync(card);
            return MapToCardResponse(card);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error blocking/unblocking card");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to block/unblock card"));
        }
    }

    public override async Task<AuthorizeTransactionResponse> AuthorizeTransaction(AuthorizeTransactionRequest request, ServerCallContext context)
    {
        try
        {
            // In a real system, we would validate the card number and CVV against a secure vault
            // For demo purposes, we'll just check if the card exists and is active
            var card = await _cardRepository.GetByIdAsync(Guid.Parse(request.CardNumber));
            if (card == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Card not found"));

            if (!card.IsActive)
                return new AuthorizeTransactionResponse
                {
                    IsApproved = false,
                    DeclineReason = "Card is not active"
                };

            if (card.IsBlocked)
                return new AuthorizeTransactionResponse
                {
                    IsApproved = false,
                    DeclineReason = "Card is blocked"
                };

            var amount = new DomainMoney(request.Amount.Amount / 100m, request.Amount.Currency);

            try
            {
                card.AuthorizeTransaction(request.MerchantName, amount);
                await _cardRepository.UpdateAsync(card);

                return new AuthorizeTransactionResponse
                {
                    IsApproved = true,
                    AuthorizationCode = Guid.NewGuid().ToString("N").Substring(0, 6).ToUpper()
                };
            }
            catch (InvalidOperationException ex)
            {
                return new AuthorizeTransactionResponse
                {
                    IsApproved = false,
                    DeclineReason = ex.Message
                };
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing transaction");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to process transaction"));
        }
    }

    public override async Task<CardResponse> GetCard(GetCardRequest request, ServerCallContext context)
    {
        try
        {
            var card = await _cardRepository.GetByIdAsync(Guid.Parse(request.CardId));
            if (card == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Card not found"));

            return MapToCardResponse(card);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving card");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve card"));
        }
    }

    public override async Task<GetAccountCardsResponse> GetAccountCards(GetAccountCardsRequest request, ServerCallContext context)
    {
        try
        {
            var cards = await _cardRepository.GetByAccountIdAsync(Guid.Parse(request.AccountId));
            var response = new GetAccountCardsResponse();
            response.Cards.AddRange(cards.Select(MapToCardResponse));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving account cards");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve account cards"));
        }
    }

    private static CardResponse MapToCardResponse(Card card)
    {
        return new CardResponse
        {
            CardId = card.Id.ToString(),
            AccountId = card.AccountId.ToString(),
            CardType = card.Type.ToString().ToUpper(),
            MaskedCardNumber = MaskCardNumber(card.CardNumber),
            ExpiryDate = card.ExpiryDate.ToString("MM/yy"),
            Status = card.IsBlocked ? "BLOCKED" : (card.IsActive ? "ACTIVE" : "INACTIVE"),
            CreditLimit = new Money { Amount = (long)(card.CreditLimit.Amount * 100), Currency = card.CreditLimit.Currency },
            CreatedAt = card.CreatedAt.ToString("O"),
            LastModifiedAt = card.LastUsedAt?.ToString("O") ?? card.CreatedAt.ToString("O")
        };
    }

    private static string MaskCardNumber(string cardNumber)
    {
        return $"****{cardNumber[^4..]}";
    }
} 