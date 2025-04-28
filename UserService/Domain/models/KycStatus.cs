using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.models
{
    public enum KycStatus
    {
        Pending,
        InProgress,
        Verified,
        Rejected
    }
}
