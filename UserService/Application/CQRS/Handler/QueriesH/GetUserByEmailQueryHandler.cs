using Application.CQRS.DTO;
using Application.CQRS.Queries;
using Domain.Interface;
using MediatR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.CQRS.Handler.QueriesH
{
    public class GetUserByEmailQueryHandler(IUserRepository userRepository) : IRequestHandler<GetUserByEmailQuery, UserDto>
    {
        private readonly IUserRepository _userRepository = userRepository;

        public async Task<UserDto?> Handle(GetUserByEmailQuery request, CancellationToken cancellationToken)
        {
            var user = await _userRepository.GetByEmailAsync(request.Email);
            return user == null ? null : new UserDto(
                user.Id,
                user.Username,
                user.Email,
                user.CreatedAt
            );
        }
    }
}
