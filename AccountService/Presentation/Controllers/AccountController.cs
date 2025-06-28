using Application.CQRS.Commands.BlockAccount;
using Application.CQRS.Commands.CloseAccount;
using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Commands.UpdateBalance;
using Application.CQRS.DTO;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.Queries.GetUserAccounts;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

//can be removed if not using gRPC reflection
namespace Presentation.Controllers;

[ApiController]
[Route("accounts")]
[Authorize]
public class AccountController : ControllerBase
{
    private readonly IMediator _mediator;

    public AccountController(IMediator mediator)
    {
        _mediator = mediator;
    }

    [HttpPost]
    public async Task<ActionResult<Guid>> CreateAccount(CreateAccountCommand command)
    {
        var accountId = await _mediator.Send(command);
        return CreatedAtAction(nameof(GetAccount), new { id = accountId }, accountId);
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<AccountDto>> GetAccount(Guid id)
    {
        var account = await _mediator.Send(new GetAccountQuery(id));
        if (account == null)
            return NotFound();

        return account;
    }

    [HttpGet("user/{userId:guid}")]
    public async Task<ActionResult<IEnumerable<AccountDto>>> GetUserAccounts(Guid userId)
    {
        var accounts = await _mediator.Send(new GetUserAccountsQuery(userId));
        return Ok(accounts);
    }

    [HttpPut("{id:guid}/balance")]
    public async Task<IActionResult> UpdateBalance(Guid id, UpdateBalanceCommand command)
    {
        if (id != command.AccountId)
            return BadRequest();

        var success = await _mediator.Send(command);
        if (!success)
            return NotFound();

        return NoContent();
    }

    [HttpPut("{id:guid}/close")]
    public async Task<IActionResult> CloseAccount(Guid id)
    {
        var success = await _mediator.Send(new CloseAccountCommand(id));
        if (!success)
            return NotFound();

        return NoContent();
    }

    [HttpPut("{id:guid}/block")]
    public async Task<IActionResult> BlockAccount(Guid id, [FromBody] bool block)
    {
        var success = await _mediator.Send(new BlockAccountCommand(id, block));
        if (!success)
            return NotFound();

        return NoContent();
    }
} 