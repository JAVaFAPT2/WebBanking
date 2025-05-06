using Domain.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.Interface
{
    public interface IKycDocumentRepository
    {
        Task AddAsync(KycDocument kycDocument);
        Task<KycDocument?> GetByIdAsync(Guid id);
        Task DeleteAsync(Guid id);
    }
}
