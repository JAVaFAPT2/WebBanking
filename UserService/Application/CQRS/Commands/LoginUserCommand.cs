using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Application.CQRS.DTO;
using MediatR;

namespace Application.CQRS.Commands
{
    public record LoginUserCommand(string Username, string Password) : IRequest<LoginResponseDto>;
}
