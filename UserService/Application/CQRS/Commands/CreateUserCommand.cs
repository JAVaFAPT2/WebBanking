using MediatR;
using System;
using Application.CQRS.DTO;

namespace Application.CQRS.Commands
{

    public record CreateUserCommand(
            string Username,
            string Email,
            string Password,
            string FirstName,
            string LastName,
            string PhoneNumber,
            DateTime DateOfBirth,
            AddressC Address
        ) : IRequest<Guid>;
}