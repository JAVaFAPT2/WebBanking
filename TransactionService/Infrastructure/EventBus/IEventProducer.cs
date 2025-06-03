using System.Threading.Tasks;

namespace TransactionService.Infrastructure.EventBus;

public interface IEventProducer<TKey, TValue>
{
    Task ProduceAsync(string topic, TKey key, TValue value);
}

// Generic interface for when key/value types are not critical for the interface definition itself
public interface IEventProducer 
{
    Task ProduceAsync<TKey, TValue>(string topic, TKey key, TValue value);
} 