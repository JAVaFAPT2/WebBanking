using Domain.models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Domain.Interface
{
    public interface IKycDocumentRepository
    {
        Task AddAsync(KycDocument kycDocument);
        Task<KycDocument?> GetByIdAsync(Guid id);
        Task<IEnumerable<KycDocument>> GetByUserIdAsync(Guid userId);
        Task UpdateAsync(KycDocument kycDocument);
        Task DeleteAsync(Guid id);
    }
}