using Domain.models;
using MediatR;

namespace Application.CQRS.Commands
{
    public record VerifyKycCommand(Guid DocumentId, string Status, string VerifierNotes) : IRequest<bool>;
}