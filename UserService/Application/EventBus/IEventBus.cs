using System.Threading.Tasks;

namespace Application.EventBus
{
    /// <summary>
    /// Abstraction for publishing domain events.
    /// </summary>
    public interface IEventBus
    {
        Task PublishAsync<TEvent>(TEvent @event)
            where TEvent : class;
    }
}

