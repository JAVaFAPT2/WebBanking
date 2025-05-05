using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;

namespace Application.Commands
{ 
    public record CreateAccountCommand(Guid UserId, string AccountNumber, decimal InitialBalance, string AccountType) : IRequest<Guid>;
}
