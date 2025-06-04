using Application.CQRS.DTO;
using MediatR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.CQRS.Queries
{
    public record GetUserByIdQuery(Guid UserId) : IRequest<UserDto>;

}
