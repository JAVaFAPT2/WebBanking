using FluentValidation;

namespace NotificationService.Application.CQRS.Commands.SendSms;

public class SendSmsCommandValidator : AbstractValidator<SendSmsCommand>
{
    public SendSmsCommandValidator()
    {
        RuleFor(x => x.ToNumber)
            .NotEmpty().WithMessage("Recipient phone number is required.")
            // Basic E.164 format check (can be enhanced)
            .Matches(@"^\+?[1-9]\d{1,14}$").WithMessage("Invalid phone number format. Expected E.164 format (e.g., +12125551234).");

        RuleFor(x => x.Message)
            .NotEmpty().WithMessage("SMS message body is required.")
            .MaximumLength(1600).WithMessage("SMS message cannot exceed 1600 characters (for multi-part SMS, though provider limits may vary)."); // Common max for concatenated SMS

        // Optional: Validate FromNumber if provided and if you have specific format rules for it
        When(x => !string.IsNullOrEmpty(x.FromNumber), () =>
        {
            RuleFor(x => x.FromNumber)
                .MaximumLength(15).WithMessage("Sender ID/number is too long."); // Adjust length as per provider constraints
        });
    }
} 