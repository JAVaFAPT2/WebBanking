using FluentValidation;
using System.Linq;

namespace NotificationService.Application.CQRS.Commands.SendPush;

public class SendPushNotificationCommandValidator : AbstractValidator<SendPushNotificationCommand>
{
    public SendPushNotificationCommandValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty().WithMessage("Push notification title is required.")
            .MaximumLength(100).WithMessage("Title cannot exceed 100 characters.");

        RuleFor(x => x.Body)
            .NotEmpty().WithMessage("Push notification body is required.")
            .MaximumLength(2000).WithMessage("Body cannot exceed 2000 characters.");

        // Ensure either DeviceToken or DeviceTokens is provided, but not both, and not neither.
        RuleFor(x => x)
            .Must(x => !string.IsNullOrWhiteSpace(x.DeviceToken) || (x.DeviceTokens != null && x.DeviceTokens.Any()))
            .WithMessage("Either a single DeviceToken or a list of DeviceTokens must be provided.")
            .Must(x => !( !string.IsNullOrWhiteSpace(x.DeviceToken) && (x.DeviceTokens != null && x.DeviceTokens.Any()) ))
            .WithMessage("Provide either a single DeviceToken or a list of DeviceTokens, not both.");

        When(x => !string.IsNullOrWhiteSpace(x.DeviceToken), () =>
        {
            RuleFor(x => x.DeviceToken)
                .NotEmpty().WithMessage("Device token cannot be empty when provided as a single token.")
                .MaximumLength(4096).WithMessage("Device token is too long."); // Max length for FCM tokens, adjust if needed
        });

        When(x => x.DeviceTokens != null && x.DeviceTokens.Any(), () =>
        {
            RuleFor(x => x.DeviceTokens)
                .Must(tokens => tokens.All(t => !string.IsNullOrWhiteSpace(t)))
                .WithMessage("All device tokens in the list must be non-empty.")
                .ForEach(tokenRule =>
                {
                    tokenRule.MaximumLength(4096).WithMessage("A device token in the list is too long.");
                });
        });

        // Optional: Validate DataPayload if there are constraints on keys or values
        When(x => x.DataPayload != null && x.DataPayload.Any(), () =>
        {
            // Example: Ensure keys and values are not excessively long
            RuleForEach(x => x.DataPayload)
                .ChildRules(payloadEntry => {
                    payloadEntry.RuleFor(kvp => kvp.Key)
                        .NotEmpty().WithMessage("Data payload key cannot be empty.")
                        .MaximumLength(50).WithMessage("Data payload key is too long.");
                    payloadEntry.RuleFor(kvp => kvp.Value)
                        .NotEmpty().WithMessage("Data payload value cannot be empty.")
                        .MaximumLength(255).WithMessage("Data payload value is too long.");
                });
        });
    }
} 