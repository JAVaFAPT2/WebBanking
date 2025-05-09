using Application.CQRS.Commands;
using Domain.Interface;
using MediatR;

namespace Application.CQRS.Handler
{
    public class UpdateKycDocumentStatusCommandHandler : IRequestHandler<UpdateKycDocumentStatusCommand, bool>
    {
        private readonly IKycService _kycService;

        public UpdateKycDocumentStatusCommandHandler(IKycService kycService)
        {
            _kycService = kycService ?? throw new ArgumentNullException(nameof(kycService));
        }

        public async Task<bool> Handle(UpdateKycDocumentStatusCommand request, CancellationToken cancellationToken)
        {
            if (request.DocumentId == Guid.Empty)
            {
                throw new ArgumentException("Document ID cannot be empty", nameof(request.DocumentId));
            }

            if (string.IsNullOrWhiteSpace(request.Status))
            {
                throw new ArgumentException("Status cannot be empty", nameof(request.Status));
            }

            await _kycService.UpdateKycDocumentStatusAsync(request.DocumentId, request.Status, request.VerifierNotes);
            return true;
        }
    }
}