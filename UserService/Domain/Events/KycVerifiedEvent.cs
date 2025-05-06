using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.models;

namespace Domain.Events
{
    public record KycVerifiedEvent(Guid UserId,KycStatus Status);

}
