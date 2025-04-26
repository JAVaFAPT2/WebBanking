using Application.CQRS.Commands;
using Domain.Interface;
using MediatR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.CQRS.Handler
{
    public class UpdateUserCommandHandler(IUserRepository userRepository) : IRequestHandler<UpdateUserCommand, Unit>
    {
        private readonly IUserRepository _userRepository = userRepository;

        public async Task<Unit> Handle(UpdateUserCommand request, CancellationToken cancellationToken)
        {
            var user = await _userRepository.GetByIdAsync(request.UserId);
            if (user == null)
            {
                throw new Exception("User not found");
            }

            user.Update(request.Username, request.Email);
            await _userRepository.UpdateAsync(user);
            return Unit.Value;
        }
    }
}
