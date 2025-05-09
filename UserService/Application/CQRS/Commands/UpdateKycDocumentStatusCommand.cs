using MediatR;

namespace Application.CQRS.Commands;

public record UpdateKycDocumentStatusCommand(Guid DocumentId, string Status,string VerifierNotes) : IRequest<bool>;
