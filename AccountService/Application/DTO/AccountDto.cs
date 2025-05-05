using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.DTo
{
    public record AccountDto(Guid AccountId, string AccountNumber, decimal Balance, string AccountType, DateTime CreatedAt);
}
