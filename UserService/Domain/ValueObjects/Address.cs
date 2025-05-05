using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;

namespace Domain.ValueObjects
{
    [Owned]
    public class Address
    {
        [Required]
        [StringLength(100)]
        [Column(TypeName = "nvarchar(100)")]
        public string Street { get; }

        [Required]
        [StringLength(50)]
        [Column(TypeName = "nvarchar(50)")]
        public string City { get; }

        [Required]
        [StringLength(50)]
        [Column(TypeName = "nvarchar(50)")]
        public string State { get; }

        [Required]
        [StringLength(20)]
        [Column(TypeName = "nvarchar(20)")]
        public string ZipCode { get; }

        [Required]
        [StringLength(50)]
        [Column(TypeName = "nvarchar(50)")]
        public string Country { get; }

        public Address() { } // For ORM

        public Address(string street, string city, string state, string zipCode, string country)
        {
            if (string.IsNullOrWhiteSpace(street))
                throw new ArgumentNullException(nameof(street));
            if (string.IsNullOrWhiteSpace(city))
                throw new ArgumentNullException(nameof(city));
            if (string.IsNullOrWhiteSpace(state))
                throw new ArgumentNullException(nameof(state));
            if (string.IsNullOrWhiteSpace(zipCode))
                throw new ArgumentNullException(nameof(zipCode));
            if (string.IsNullOrWhiteSpace(country))
                throw new ArgumentNullException(nameof(country));

            Street = street;
            City = city;
            State = state;
            ZipCode = zipCode;
            Country = country;
        }

        // Value Object equality methods
        public override bool Equals(object obj)
        {
            if (obj is not Address other)
                return false;

            return Street == other.Street &&
                   City == other.City &&
                   State == other.State &&
                   ZipCode == other.ZipCode &&
                   Country == other.Country;
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(Street, City, State, ZipCode, Country);
        }
    }
}
