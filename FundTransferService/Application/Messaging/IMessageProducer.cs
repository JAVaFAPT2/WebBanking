using System.Threading.Tasks;
using System.Threading;

namespace FundTransferService.Application.Messaging
{
    public interface IMessageProducer : System.IDisposable
    {
        Task ProduceAsync<TKey, TValue>(string topic, TKey key, TValue value, CancellationToken cancellationToken = default);
    }
}
