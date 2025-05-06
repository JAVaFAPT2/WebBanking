using Domain.Interface;
using Domain.models;
using System;
using System.Threading.Tasks;

namespace Infrastructure.Persistence.Service
{
    public class KycService : IKycService
    {
        private readonly IUserRepository _userRepository;
        private readonly IKycDocumentRepository _kycDocumentRepository;

        public KycService(IUserRepository userRepository, IKycDocumentRepository kycDocumentRepository)
        {
            _userRepository = userRepository;
            _kycDocumentRepository = kycDocumentRepository;
        }

        public async Task VerifyKycAsync(Guid userId, bool isVerified)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
            {
                throw new Exception($"User with ID {userId} not found");
            }

            user.UpdateKycStatus(isVerified ? KycStatus.Verified : KycStatus.Rejected);
            await _userRepository.UpdateAsync(user);
        }

        public async Task SaveKycDocumentAsync(KycDocument kycDocument)
        {
            if (kycDocument == null)
            {
                throw new ArgumentNullException(nameof(kycDocument));
            }

            await _kycDocumentRepository.AddAsync(kycDocument);
        }
    }
}