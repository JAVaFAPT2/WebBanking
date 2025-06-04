using System;

namespace Domain.Events
{
    /// <summary>
    /// Event that is raised when a user's profile is updated
    /// </summary>
    public class UserUpdatedEvent
    {
        /// <summary>
        /// The unique identifier of the updated user
        /// </summary>
        public Guid UserId { get; }

        /// <summary>
        /// The username of the updated user
        /// </summary>
        public string Username { get; }

        /// <summary>
        /// The email address of the updated user
        /// </summary>
        public string Email { get; }

        /// <summary>
        /// The timestamp when the user was updated
        /// </summary>
        public DateTime UpdatedAt { get; }

        /// <summary>
        /// Creates a new UserUpdatedEvent
        /// </summary>
        /// <param name="userId">The unique identifier of the updated user</param>
        /// <param name="username">The username of the updated user</param>
        /// <param name="email">The email address of the updated user</param>
        /// <param name="updatedAt">The timestamp when the user was updated</param>
        public UserUpdatedEvent(Guid userId, string username, string email, DateTime updatedAt)
        {
            UserId = userId;
            Username = username;
            Email = email;
            UpdatedAt = updatedAt;
        }
    }
}