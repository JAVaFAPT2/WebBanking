using System.Threading.Tasks;

namespace FundTransferService.Application.Messaging
{
    public interface IMessageHandler<TKey, TValue>
    {
        Task HandleAsync(TKey key, TValue value, CancellationToken cancellationToken);
    }
} 