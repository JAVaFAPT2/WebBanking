using Application.CQRS.Queries;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler.QueriesH
{
    public class GetKycDocumentByUserIdHandler : IRequestHandler<GetKycDocumentByUserIdQueries, IEnumerable<KycDocument>>
    {
        private readonly IKycDocumentRepository _repository;

        public GetKycDocumentByUserIdHandler(IKycDocumentRepository repository)
        {
            _repository = repository;
        }

        public async Task<IEnumerable<KycDocument>> Handle(GetKycDocumentByUserIdQueries request, CancellationToken cancellationToken)
        {
            // If treating ID as document ID (single item expected)
            var document = await _repository.GetByIdAsync(request.UserId);

            if (document == null)
                return Enumerable.Empty<KycDocument>();

            return new List<KycDocument> { document }; // Wrap in list
        }
    }

}