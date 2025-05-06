using Domain.Interface;
using Domain.models;
using Infrastructure.Persistence.DBContext;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence.Repositories
{
    public class KycDocumentRepository(ApplicationDbContext dbContext) : IKycDocumentRepository
    {
        public async Task AddAsync(KycDocument kycDocument)
        {
            if (kycDocument == null)
            {
                throw new ArgumentNullException(nameof(kycDocument));
            }

            await dbContext.KycDocuments.AddAsync(kycDocument);
            await dbContext.SaveChangesAsync();
        }

        public async Task<KycDocument?> GetByIdAsync(Guid id)
        {
            return await dbContext.KycDocuments.FirstOrDefaultAsync(doc => doc.Id == id);
        }

        public async Task DeleteAsync(Guid id)
        {
            var kycDocument = await GetByIdAsync(id);
            if (kycDocument != null)
            {
                dbContext.KycDocuments.Remove(kycDocument);
                await dbContext.SaveChangesAsync();
            }
        }
    }
}
