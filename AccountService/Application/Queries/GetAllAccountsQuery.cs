using Application.DTo;
using MediatR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.Queries
{
    public record GetAllAccountsQuery : IRequest<List<AccountDto>>;
}
