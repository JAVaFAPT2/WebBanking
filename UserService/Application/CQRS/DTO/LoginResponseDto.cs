using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.CQRS.DTO
{
   public record LoginResponseDto(string Token,Guid UserId,string UserName);
}
