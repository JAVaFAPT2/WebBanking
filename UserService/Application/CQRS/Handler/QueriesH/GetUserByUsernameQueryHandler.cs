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
    public class GetUserByUsernameQueryHandler : IRequestHandler<GetUserByUsernameQuery, UserDto>
    {
        private readonly IUserRepository _userRepository;

        public GetUserByUsernameQueryHandler(IUserRepository userRepository)
        {
            _userRepository = userRepository;
        }

        public async Task<UserDto> Handle(GetUserByUsernameQuery request, CancellationToken cancellationToken)
        {
            var user = await _userRepository.GetByUsernameAsync(request.UserName);
            if (user == null) return null;

            return new UserDto(user.Id, user.Username, user.Email, user.CreatedAt);
        }
    }

}
