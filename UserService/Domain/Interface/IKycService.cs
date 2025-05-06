using Domain.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.Interface
{
    public interface IKycService
    {
        Task VerifyKycAsync(Guid userId, bool isVerified);
        Task SaveKycDocumentAsync(KycDocument kycDocument);
    }
}
