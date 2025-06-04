using Domain.models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Domain.Interface
{
    public interface IKycService
    {
        Task<bool> VerifyKycAsync(Guid userId, bool isVerified, string verificationNotes = null);
        Task<Guid> SaveKycDocumentAsync(KycDocument kycDocument);
        Task<KycDocument> GetKycDocumentByIdAsync(Guid documentId);
        Task<IEnumerable<KycDocument>> GetKycDocumentsByUserIdAsync(Guid userId);
        Task UpdateKycDocumentStatusAsync(Guid documentId, string status, string verifierNotes = null);
    }
}