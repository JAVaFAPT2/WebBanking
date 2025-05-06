using MediatR;
using System;
using Application.CQRS.DTO;
using Domain.models;

namespace Application.CQRS.Commands
{
    public record UpdateUserCommand(
            Guid UserId,
            string FirstName,
            string LastName,
            string PhoneNumber,
            AddressC Address,
            KycStatus KycStatus,
            string? KycDocumentType,
            string? KycDocumentPath
        ) : IRequest<Unit>;
}