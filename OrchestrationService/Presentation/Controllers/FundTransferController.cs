using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using OrchestrationService.Application.Services;
using OrchestrationService.Domain.Interfaces;
using OrchestrationService.Domain.Models;
using OrchestrationService.Presentation.Models;

namespace OrchestrationService.Presentation.Controllers;

/// <summary>
/// Controller for fund transfer operations
/// </summary>
[ApiController]
[Route("fundtransfer")]
public class FundTransferController : ControllerBase
{
    private readonly FundTransferOrchestrator _orchestrator;
    private readonly ISagaRepository<FundTransferSaga> _repository;
    private readonly ILogger<FundTransferController> _logger;

    /// <summary>
    /// Constructor
    /// </summary>
    public FundTransferController(
        FundTransferOrchestrator orchestrator,
        ISagaRepository<FundTransferSaga> repository,
        ILogger<FundTransferController> logger)
    {
        _orchestrator = orchestrator ?? throw new ArgumentNullException(nameof(orchestrator));
        _repository = repository ?? throw new ArgumentNullException(nameof(repository));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <summary>
    /// Start a new fund transfer
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(FundTransferResponse), 202)]
    [ProducesResponseType(typeof(ProblemDetails), 400)]
    public async Task<IActionResult> StartFundTransfer(
        [FromBody] FundTransferRequest request,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            _logger.LogInformation("Starting fund transfer from account {SourceAccountId} to {DestinationAccountId} for {Amount} {Currency}",
                request.SourceAccountId, request.DestinationAccountId, request.Amount, request.Currency);

            var transactionId = await _orchestrator.StartFundTransferAsync(
                request.UserId,
                request.SourceAccountId,
                request.DestinationAccountId,
                request.Amount,
                request.Currency,
                request.Reference,
                cancellationToken);

            var response = new FundTransferResponse
            {
                TransactionId = transactionId,
                Status = "Accepted",
                Message = "Fund transfer process has been initiated"
            };

            // Return 202 Accepted since this is an asynchronous operation
            return Accepted(response);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error starting fund transfer");
            throw;
        }
    }

    /// <summary>
    /// Get the status of a fund transfer
    /// </summary>
    [HttpGet("{transactionId}")]
    [ProducesResponseType(typeof(FundTransferStatusResponse), 200)]
    [ProducesResponseType(typeof(ProblemDetails), 404)]
    public async Task<IActionResult> GetFundTransferStatus(
        string transactionId,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting status for fund transfer {TransactionId}", transactionId);

        var saga = await _repository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Fund transfer not found",
                Detail = $"No fund transfer with transaction ID {transactionId} was found"
            });
        }

        var response = new FundTransferStatusResponse
        {
            TransactionId = transactionId,
            Status = saga.State.ToString(),
            CreatedAt = saga.CreatedAt,
            LastUpdatedAt = saga.LastUpdatedAt,
            CurrentStep = saga.CurrentStep,
            TotalSteps = saga.TotalSteps,
            SourceAccountId = saga.SourceAccountId,
            DestinationAccountId = saga.DestinationAccountId,
            Amount = saga.Amount,
            Currency = saga.Currency,
            ErrorMessage = saga.ErrorMessage
        };

        return Ok(response);
    }

    /// <summary>
    /// Get all fund transfers for a user
    /// </summary>
    [HttpGet("user/{userId}")]
    [ProducesResponseType(typeof(IEnumerable<FundTransferStatusResponse>), 200)]
    public async Task<IActionResult> GetUserFundTransfers(
        string userId,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting fund transfers for user {UserId}", userId);

        var sagas = await _repository.GetByUserIdAsync(userId, cancellationToken);
        var response = new List<FundTransferStatusResponse>();

        foreach (var saga in sagas)
        {
            response.Add(new FundTransferStatusResponse
            {
                TransactionId = saga.TransactionId,
                Status = saga.State.ToString(),
                CreatedAt = saga.CreatedAt,
                LastUpdatedAt = saga.LastUpdatedAt,
                CurrentStep = saga.CurrentStep,
                TotalSteps = saga.TotalSteps,
                SourceAccountId = saga.SourceAccountId,
                DestinationAccountId = saga.DestinationAccountId,
                Amount = saga.Amount,
                Currency = saga.Currency,
                ErrorMessage = saga.ErrorMessage
            });
        }

        return Ok(response);
    }
} 