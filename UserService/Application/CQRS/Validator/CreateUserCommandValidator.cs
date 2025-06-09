using Application.CQRS.Commands;
using FluentValidation;

namespace Application.CQRS.Validator
{
    public class CreateUserCommandValidator : AbstractValidator<CreateUserCommand>
    {
        public CreateUserCommandValidator()
        {
            RuleFor(x => x.Username).NotEmpty().MaximumLength(100);
            RuleFor(x => x.Email).NotEmpty().EmailAddress().MaximumLength(100);
            RuleFor(x => x.Password).NotEmpty().MinimumLength(8);
            RuleFor(x => x.FirstName).NotEmpty().MaximumLength(50);
            RuleFor(x => x.LastName).NotEmpty().MaximumLength(50);
            RuleFor(x => x.PhoneNumber).NotEmpty().MaximumLength(20);
            RuleFor(x => x.DateOfBirth).NotEmpty();
            RuleFor(x => x.Address).NotNull();
            RuleFor(x => x.Address.Street).NotEmpty();
            RuleFor(x => x.Address.City).NotEmpty();
            RuleFor(x => x.Address.State).NotEmpty();
            RuleFor(x => x.Address.ZipCode).NotEmpty();
            RuleFor(x => x.Address.Country).NotEmpty();
        }
    }
}
