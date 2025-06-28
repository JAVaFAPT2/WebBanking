using Domain.Interface;
using Domain.models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Infrastructure.Persistence.Service
{
    public class KycService : IKycService
    {
        private readonly IUserRepository _userRepository;
        private readonly IKycDocumentRepository _kycDocumentRepository;

        public KycService(IUserRepository userRepository, IKycDocumentRepository kycDocumentRepository)
        {
            _userRepository = userRepository ?? throw new ArgumentNullException(nameof(userRepository));
            _kycDocumentRepository = kycDocumentRepository ?? throw new ArgumentNullException(nameof(kycDocumentRepository));
        }

        public async Task<bool> VerifyKycAsync(Guid userId, bool isVerified, string verificationNotes = null)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
            {
                throw new Exception($"User with ID {userId} not found");
            }

            user.UpdateKycStatus(isVerified ? KycStatus.Verified : KycStatus.Rejected);
            user.KycVerificationDate = DateTime.UtcNow;
            user.KycVerificationNotes = verificationNotes;

            await _userRepository.UpdateAsync(user);
            return true;
        }

        public async Task<Guid> SaveKycDocumentAsync(KycDocument kycDocument)
        {
            if (kycDocument == null)
            {
                throw new ArgumentNullException(nameof(kycDocument));
            }

            // Set default values if not provided
            if (kycDocument.Id == Guid.Empty)
            {
                kycDocument.Id = Guid.NewGuid();
            }

            if (kycDocument.SubmissionDate == default)
            {
                kycDocument.SubmissionDate = DateTime.UtcNow;
            }

            if (string.IsNullOrEmpty(kycDocument.Status))
            {
                kycDocument.Status = "Pending";
            }

            // Verify user exists
            var user = await _userRepository.GetByIdAsync(kycDocument.UserId);
            if (user == null)
            {
                throw new Exception($"User with ID {kycDocument.UserId} not found");
            }

            // Update user KYC status to pending if it's currently unverified
            if (user.KycStatus == KycStatus.Unverified)
            {
                user.UpdateKycStatus(KycStatus.Pending);
                await _userRepository.UpdateAsync(user);
            }

            await _kycDocumentRepository.AddAsync(kycDocument);
            return kycDocument.Id;
        }

        public async Task<KycDocument> GetKycDocumentByIdAsync(Guid documentId)
        {
            var document = await _kycDocumentRepository.GetByIdAsync(documentId);
            if (document == null)
            {
                throw new Exception($"KYC document with ID {documentId} not found");
            }

            return document;
        }

        public async Task<IEnumerable<KycDocument>> GetKycDocumentsByUserIdAsync(Guid userId)
        {
            // First check if user exists
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
            {
                throw new Exception($"User with ID {userId} not found");
            }

            return await _kycDocumentRepository.GetByUserIdAsync(userId);
        }

        public async Task UpdateKycDocumentStatusAsync(Guid documentId, string status, string verifierNotes = null)
        {
            var document = await _kycDocumentRepository.GetByIdAsync(documentId);
            if (document == null)
            {
                throw new Exception($"KYC document with ID {documentId} not found");
            }

            document.Status = status;
            document.VerificationDate = DateTime.UtcNow;
            document.VerifierNotes = verifierNotes;

            await _kycDocumentRepository.UpdateAsync(document);

            // If document is rejected or approved, update user KYC status accordingly
            if (status == "Approved" || status == "Rejected")
            {
                var user = await _userRepository.GetByIdAsync(document.UserId);
                if (user != null)
                {
                    user.UpdateKycStatus(status == "Approved" ? KycStatus.Verified : KycStatus.Rejected);
                    user.KycVerificationDate = DateTime.UtcNow;
                    user.KycVerificationNotes = verifierNotes;
                    await _userRepository.UpdateAsync(user);
                }
            }
        }
    }
}
