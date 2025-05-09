using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.models;

namespace Domain.Events
{
    public record KycVerifiedEvent(string DocumentType, string DocumentNumber, string IssuingCountry,DateTime? ExpiryDate, string DocumentPath, string Status, DateTime? VerifiedAt,Guid UserId);

}
