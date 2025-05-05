using MediatR;
using System;

namespace Application.CQRS.Commands
{
    public record AddressC(
            string Street,
            string City,
            string State,
            string ZipCode,
            string Country
        );

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