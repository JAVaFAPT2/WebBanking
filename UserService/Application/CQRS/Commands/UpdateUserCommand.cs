using MediatR;
using System;

namespace Application.CQRS.Commands
{
    public record UpdateUserCommand(
            Guid UserId,
            string FirstName,
            string LastName,
            string PhoneNumber,
            string Street,
            string City,
            string State,
            string ZipCode,
            string Country
        ) : IRequest<Unit>;
}