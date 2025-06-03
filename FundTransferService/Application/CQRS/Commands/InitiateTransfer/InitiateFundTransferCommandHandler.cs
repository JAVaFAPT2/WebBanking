using MediatR;
using FundTransferService.Domain.Interfaces;
using FundTransferService.Domain.Entities;
using Microsoft.Extensions.Logging;
using FundTransferService.Domain.Events;
using FundTransferService.Domain.ValueObjects;
using System;
using System.Threading;
using System.Threading.Tasks;
using FundTransferService.Domain.Configuration;
using Microsoft.Extensions.Options;
using FundTransferService.Application.Messaging;

namespace FundTransferService.Application.CQRS.Commands.InitiateTransfer;

public class InitiateFundTransferCommandHandler : IRequestHandler<InitiateFundTransferCommand, Guid>
{
    private readonly IFundTransferRepository _fundTransferRepository;
    private readonly ILogger<InitiateFundTransferCommandHandler> _logger;
    private readonly IMessageProducer _messageProducer;
    private readonly KafkaSettings _kafkaSettings;

    public InitiateFundTransferCommandHandler(
        IFundTransferRepository fundTransferRepository, 
        ILogger<InitiateFundTransferCommandHandler> logger,
        IMessageProducer messageProducer,
        IOptions<KafkaSettings> kafkaSettingsOptions)
    {
        _fundTransferRepository = fundTransferRepository ?? throw new ArgumentNullException(nameof(fundTransferRepository));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _messageProducer = messageProducer ?? throw new ArgumentNullException(nameof(messageProducer));
        _kafkaSettings = kafkaSettingsOptions.Value ?? throw new ArgumentNullException(nameof(kafkaSettingsOptions));
    }

    public async Task<Guid> Handle(InitiateFundTransferCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Initiating fund transfer from {FromAccountId} to {ToAccountId} for {Amount} {Currency}",
            request.FromAccountId, request.ToAccountId, request.Amount.Amount, request.Amount.Currency);

        // Basic validation (more complex validation should be in the validator)
        if (request.FromAccountId == Guid.Empty || request.ToAccountId == Guid.Empty || request.Amount.Amount <= 0)
        {
            _logger.LogError("Invalid fund transfer request parameters.");
            throw new ArgumentException("Invalid fund transfer request parameters.");
        }

        var transfer = FundTransfer.Create(
            request.FromAccountId,
            request.ToAccountId,
            request.Amount,
            request.ReferenceNumber
        );

        await _fundTransferRepository.AddAsync(transfer, cancellationToken);
        _logger.LogInformation("Fund transfer entity {TransferId} created and persisted with status {Status}.", transfer.Id, transfer.Status);

        var eventToPublish = new FundTransferInitiatedEvent(
            transfer.Id,
            transfer.FromAccountId,
            transfer.ToAccountId,
            transfer.Amount,
            transfer.ReferenceNumber ?? string.Empty,
            transfer.CreatedAt,
            transfer.Status
        );

        try
        {
            string topic = _kafkaSettings.ProducerTopic;
            if (string.IsNullOrEmpty(topic))
            {
                _logger.LogError("Kafka producer topic is not configured. Cannot send FundTransferInitiatedEvent.");
            }
            else
            {
                await _messageProducer.ProduceAsync(topic, transfer.Id.ToString(), eventToPublish, cancellationToken);
                _logger.LogInformation("FundTransferInitiatedEvent for TransferId {TransferId} published to Kafka topic {Topic}.", 
                    transfer.Id, topic);
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to publish FundTransferInitiatedEvent to Kafka for TransferId {TransferId}. The fund transfer itself was successful.", transfer.Id);
        }

        return transfer.Id;
    }
} 