using Application.CQRS.Queries;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler.QueriesH
{
    public partial class GetKycDocumentByIdQueryHandler : IRequestHandler<GetKycDocumentByIdQueries, KycDocument>
    {
        private readonly IKycService _kycService;

        public GetKycDocumentByIdQueryHandler(IKycService kycService)
        {
            _kycService = kycService ?? throw new ArgumentNullException(nameof(kycService));
        }

        public async Task<KycDocument> Handle(GetKycDocumentByIdQueries request, CancellationToken cancellationToken)
        {
            if (request.DocumentId == Guid.Empty)
            {
                throw new ArgumentException("Document ID cannot be empty", nameof(request.DocumentId));
            }

            return await _kycService.GetKycDocumentByIdAsync(request.DocumentId);
        }
    }
}