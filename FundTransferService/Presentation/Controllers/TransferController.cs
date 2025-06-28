using Application.CQRS.Commands.InitiateTransfer;
using Application.DTOs;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Presentation.Controllers;

[ApiController]
[Route("transfers")]
[Authorize]
public class TransferController : ControllerBase
{
    private readonly IMediator _mediator;

    public TransferController(IMediator mediator)
    {
        _mediator = mediator;
    }

    [HttpPost]
    public async Task<IActionResult> InitiateTransfer([FromBody] InitiateFundTransferCommand command)
    {
        var result = await _mediator.Send(command);
        if (result.IsSuccess)
            return Ok(result.Value);
        return BadRequest(result.ErrorMessage);
    }
} 