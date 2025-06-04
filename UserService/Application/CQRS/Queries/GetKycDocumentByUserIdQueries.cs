using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.models;
using MediatR;

namespace Application.CQRS.Queries
{
    public record GetKycDocumentByUserIdQueries(Guid UserId) : IRequest<IEnumerable<KycDocument>>;
}
