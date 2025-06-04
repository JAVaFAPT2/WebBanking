using Domain.Interface;
using Domain.models;
using Infrastructure.Persistence.DBContext;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence.Repositories
{
    public class KycDocumentRepository : IKycDocumentRepository
    {
        private readonly ApplicationDbContext _context;

        public KycDocumentRepository(ApplicationDbContext context)
        {
            _context = context ?? throw new ArgumentNullException(nameof(context));
        }

        public async Task AddAsync(KycDocument kycDocument)
        {
            await _context.KycDocuments.AddAsync(kycDocument);
            await _context.SaveChangesAsync();
        }

        public async Task<KycDocument?> GetByIdAsync(Guid id)
        {
            return await _context.KycDocuments
                .Include(d => d.User)
                .FirstOrDefaultAsync(d => d.Id == id);
        }

        public async Task<IEnumerable<KycDocument>> GetByUserIdAsync(Guid userId)
        {
            return await _context.KycDocuments
                .Where(d => d.UserId == userId)
                .ToListAsync();
        }

        public async Task UpdateAsync(KycDocument kycDocument)
        {
            _context.KycDocuments.Update(kycDocument);
            await _context.SaveChangesAsync();
        }

        public async Task DeleteAsync(Guid id)
        {
            var document = await _context.KycDocuments.FindAsync(id);
            if (document != null)
            {
                _context.KycDocuments.Remove(document);
                await _context.SaveChangesAsync();
            }
        }
    }
}