using System;
using System.Threading.Tasks;
using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Application.CQRS.Queries;
using MediatR;
using Microsoft.AspNetCore.Mvc;
using Moq;
using NUnit.Framework;
using Presentation.Controllers;

namespace Testc
{
    public class Tests
    {
        private Mock<IMediator> _mediatorMock;
        private UserController _controller;

        [SetUp]
        public void Setup()
        {
            _mediatorMock = new Mock<IMediator>();
            _controller = new UserController(_mediatorMock.Object); // Fixed typo: *controller -> _controller, *mediatorMock -> _mediatorMock
        }

        [Test]
        public async Task CreateUser_ReturnsOkResult_WithUserId()
        {
            // Arrange
            var command = new CreateUserCommand(
                "testuser", "testuser@example.com", "hashedpassword", "Test", "User", "hashedpassword", DateTime.UtcNow,
                new AddressC("123 Street", "City", "State", "12345", "Country")
            );
            var userId = Guid.NewGuid();
            _mediatorMock.Setup(m => m.Send(It.IsAny<CreateUserCommand>(), default)).ReturnsAsync(userId);

            // Act
            var result = await _controller.CreateUser(command);

            // Assert
            var okResult = result as OkObjectResult;
            Assert.That(okResult, Is.Not.Null);

            // Let's inspect what's actually in the response
            var dictionary = okResult.Value as System.Collections.Generic.Dictionary<string, object>;

            if (dictionary != null)
            {
                // It's a dictionary
                Assert.That(dictionary.ContainsKey("UserId"), Is.True);
                Assert.That(dictionary["UserId"], Is.EqualTo(userId));
            }
            else
            {
                // It might be an anonymous object with property name casing differences
                var properties = okResult.Value.GetType().GetProperties();

                // Find the property regardless of casing
                var userIdProperty = properties.FirstOrDefault(p =>
                    string.Equals(p.Name, "UserId", StringComparison.OrdinalIgnoreCase) ||
                    string.Equals(p.Name, "userId", StringComparison.OrdinalIgnoreCase) ||
                    string.Equals(p.Name, "userid", StringComparison.OrdinalIgnoreCase));

                Assert.That(userIdProperty, Is.Not.Null, "No property found that looks like 'UserId'");
                var returnedUserId = userIdProperty.GetValue(okResult.Value);
                Assert.That(returnedUserId, Is.EqualTo(userId));
            }
        }

        [Test]
        public async Task GetUserById_ReturnsOkResult_WhenUserExists()
        {
            // Arrange
            var userId = Guid.NewGuid();
            var user = new UserDto(userId, "testuser", "testuser@example.com", DateTime.UtcNow);
            _mediatorMock.Setup(m => m.Send(It.IsAny<GetUserByIdQuery>(), default)).ReturnsAsync(user);

            // Act
            var result = await _controller.GetUserById(userId);

            // Assert
            Assert.That(result, Is.InstanceOf<OkObjectResult>());
            Assert.That(((OkObjectResult)result).Value, Is.EqualTo(user));
        }

        [Test]
        public async Task GetUserById_ReturnsNotFound_WhenUserDoesNotExist()
        {
            // Arrange
            var userId = Guid.NewGuid();
            _mediatorMock.Setup(m => m.Send(It.IsAny<GetUserByIdQuery>(), default)).ReturnsAsync((UserDto)null);

            // Act
            var result = await _controller.GetUserById(userId);

            // Assert
            Assert.That(result, Is.InstanceOf<NotFoundResult>());
        }

        [Test]
        public async Task DeleteUser_ReturnsOkResult()
        {
            // Arrange
            var userId = Guid.NewGuid();
            _mediatorMock.Setup(m => m.Send(It.IsAny<DeleteUserCommand>(), default)).ReturnsAsync(Unit.Value);

            // Act
            var result = await _controller.DeleteUser(userId);

            // Assert
            Assert.That(result, Is.InstanceOf<OkResult>());
        }
    }
}