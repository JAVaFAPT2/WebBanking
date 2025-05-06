using System;
using System.Threading.Tasks;
using MediatR;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;

public class UserControllerTests
{
    private readonly Mock<IMediator> _mediatorMock;
    private readonly UserController _controller;

    public UserControllerTests()
    {
        _mediatorMock = new Mock<IMediator>();
        _controller = new UserController(_mediatorMock.Object);
    }

    [Fact]
    public async Task CreateUser_ReturnsOkResult_WithUserId()
    {
        // Arrange
        var command = new CreateUserCommand { Username = "testuser" };
        var userId = Guid.NewGuid();
        _mediatorMock.Setup(m => m.Send(command, default)).ReturnsAsync(userId);

        // Act
        var result = await _controller.CreateUser(command);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(userId, ((dynamic)okResult.Value).UserId);
    }

    [Fact]
    public async Task GetUserById_ReturnsOkResult_WhenUserExists()
    {
        // Arrange
        var userId = Guid.NewGuid();
        var user = new { Id = userId, Username = "testuser" };
        _mediatorMock.Setup(m => m.Send(It.IsAny<GetUserByIdQuery>(), default)).ReturnsAsync(user);

        // Act
        var result = await _controller.GetUserById(userId);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(user, okResult.Value);
    }

    [Fact]
    public async Task GetUserById_ReturnsNotFound_WhenUserDoesNotExist()
    {
        // Arrange
        var userId = Guid.NewGuid();
        _mediatorMock.Setup(m => m.Send(It.IsAny<GetUserByIdQuery>(), default)).ReturnsAsync((object)null);

        // Act
        var result = await _controller.GetUserById(userId);

        // Assert
        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task DeleteUser_ReturnsOkResult()
    {
        // Arrange
        var userId = Guid.NewGuid();
        _mediatorMock.Setup(m => m.Send(It.IsAny<DeleteUserCommand>(), default)).Returns(Task.CompletedTask);

        // Act
        var result = await _controller.DeleteUser(userId);

        // Assert
        Assert.IsType<OkResult>(result);
    }
}
