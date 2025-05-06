
using Application.CQRS.Commands;
using Application.CQRS.Queries;
using Domain.Interface;
using Domain.models;
using Infrastructure.Persistence.Service;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;


namespace Presentation.Controllers;


[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "User,Admin")]
public class UserController : ControllerBase
{
    private readonly IMediator _mediator;

    public UserController(IMediator mediator)
    {
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
    }

    [HttpPost]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserCommand command)
    {
        var userId = await _mediator.Send(command);
        return Ok(new { UserId = userId });
    }
    [HttpPost("{userId}/verify-kyc")]
    public async Task<IActionResult> VerifyKyc(Guid userId, [FromBody] bool isVerified)
    {
        var command = new VerifyKycCommand(userId, isVerified);
        await _mediator.Send(command);
        return Ok(new { Message = "KYC verification initiated successfully" });
    }

    [HttpGet("{userId}")]
    public async Task<IActionResult> GetUserById(Guid userId)
    {
        var query = new GetUserByIdQuery(userId);
        var result = await _mediator.Send(query);
        return result == null ? NotFound() : Ok(result);
    }

    [HttpGet("email/{email}")]
    public async Task<IActionResult> GetUserByEmail(string email)
    {
        var query = new GetUserByEmailQuery(email);
        var result = await _mediator.Send(query);
        return result == null ? NotFound() : Ok(result);
    }

    [HttpGet]
    public async Task<IActionResult> GetAllUsers()
    {
        var query = new GetAllUsersQuery();
        var result = await _mediator.Send(query);
        return Ok(result);
    }

    [HttpPut]
    public async Task<IActionResult> UpdateUser([FromBody] UpdateUserCommand command)
    {
        await _mediator.Send(command);
        return Ok();
    }

    [HttpDelete("{userId}")]
    public async Task<IActionResult> DeleteUser(Guid userId)
    {
        var command = new DeleteUserCommand(userId);
        await _mediator.Send(command);
        return Ok();
    }
}