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
    public class GetUserByIdQueryHandler(IUserRepository userRepository) : IRequestHandler<GetUserByIdQuery, UserDto>
    {
        public async Task<UserDto> Handle(GetUserByIdQuery request, CancellationToken cancellationToken)
        {
            var user = await userRepository.GetByIdAsync(request.UserId);
            return user == null ? null : new UserDto(
                user.Id,
                user.Username,
                user.Email,
                user.CreatedAt
            );
        }
    }
}
