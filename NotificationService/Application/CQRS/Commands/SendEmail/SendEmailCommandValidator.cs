using FluentValidation;

namespace NotificationService.Application.CQRS.Commands.SendEmail;

public class SendEmailCommandValidator : AbstractValidator<SendEmailCommand>
{
    public SendEmailCommandValidator()
    {
        RuleFor(x => x.ToEmail)
            .NotEmpty().WithMessage("Recipient email address is required.")
            .EmailAddress().WithMessage("Invalid email address format.");

        RuleFor(x => x.Subject)
            .NotEmpty().WithMessage("Email subject is required.")
            .MaximumLength(255).WithMessage("Subject cannot exceed 255 characters.");

        RuleFor(x => x.HtmlBody)
            .NotEmpty().WithMessage("Email body is required.");

        When(x => !string.IsNullOrEmpty(x.FromEmail), () =>
        {
            RuleFor(x => x.FromEmail)
                .EmailAddress().WithMessage("Invalid 'From' email address format.");
        });
    }
} 