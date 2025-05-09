using Application.CQRS.Commands;
using Domain.Interface;
using MediatR;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Application.CQRS.Handler
{
    public class SaveKycDocumentCommandHandler : IRequestHandler<SaveKycDocumentCommand, Guid>
    {
        private readonly IKycService _kycService;

        public SaveKycDocumentCommandHandler(IKycService kycService)
        {
            _kycService = kycService ?? throw new ArgumentNullException(nameof(kycService));
        }

        public async Task<Guid> Handle(SaveKycDocumentCommand request, CancellationToken cancellationToken)
        {
            if (request.KycDocument == null)
            {
                throw new ArgumentNullException(nameof(request.KycDocument));
            }

            return await _kycService.SaveKycDocumentAsync(request.KycDocument);
        }
    }
}