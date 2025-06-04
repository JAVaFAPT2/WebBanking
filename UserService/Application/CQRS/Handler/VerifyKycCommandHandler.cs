using Application.CQRS.Commands;
using Application.EventBus;
using Domain.Events;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler
{
    public class VerifyKycCommandHandler : IRequestHandler<VerifyKycCommand, bool>
    {
        private readonly IKycDocumentRepository _repository;

        public VerifyKycCommandHandler(IKycDocumentRepository repository)
        {
            _repository = repository;
        }

        public async Task<bool> Handle(VerifyKycCommand request, CancellationToken cancellationToken)
        {
            var document = await _repository.GetByIdAsync(request.DocumentId);

            if (document == null)
                return false;

            // Optionally validate allowed status values here (e.g., "Verified", "Rejected")
            document.Status = request.Status;
            document.VerifierNotes = request.VerifierNotes;
            document.VerificationDate = DateTime.UtcNow;

            await _repository.UpdateAsync(document);
            return true;
        }
    }
}