using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.CQRS.DTO
{
    public record AddressC(
        string Street,
        string City,
        string State,
        string ZipCode,
        string Country
    );

}
