using System.IdentityModel.Tokens.Jwt;
using System.Net;
using System.Security.Claims;
using System.Text;
using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Domain.models;
using Infrastructure.Persistence.DBContext;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.IdentityModel.Tokens;
using Newtonsoft.Json;
using Presentation.Dto;
using Moq;
using Confluent.Kafka;
using StackExchange.Redis;

namespace Testc;

[TestFixture]
public class UserControllerTests
{
    private WebApplicationFactory<Program> _factory;
    private HttpClient _client;

    [SetUp]
    public void Setup()
    {
        var projectDir = Directory.GetCurrentDirectory();
        var contentRoot = Path.GetFullPath(Path.Combine(projectDir, "..\\..\\..\\..\\Presentation"));
        Console.WriteLine($"Current Directory: {projectDir}");
        Console.WriteLine($"Content Root: {contentRoot}");
        Console.WriteLine($"Deps File Exists: {File.Exists(Path.Combine(projectDir, "testhost.deps.json"))}");
        if (!Directory.Exists(contentRoot))
        {
            throw new DirectoryNotFoundException($"Content root directory does not exist: {contentRoot}");
        }

        // Mock Kafka Producer
        var kafkaProducerMock = new Mock<IProducer<Null, string>>();
        kafkaProducerMock.Setup(p => p.ProduceAsync(It.IsAny<string>(), It.IsAny<Message<Null, string>>(), It.IsAny<CancellationToken>()))
                        .ReturnsAsync(new DeliveryResult<Null, string>
                        {
                            Topic = "user-events",
                            Partition = new Partition(0),
                            Offset = new Offset(0),
                            Message = new Message<Null, string> { Value = "mocked" },
                            Status = PersistenceStatus.Persisted
                        });

        // Mock Redis
        var redisMock = new Mock<IConnectionMultiplexer>();
        var redisDatabaseMock = new Mock<IDatabase>();
        redisDatabaseMock.Setup(db => db.StringSetAsync(It.IsAny<RedisKey>(), It.IsAny<RedisValue>(), It.IsAny<TimeSpan?>(), It.IsAny<When>(), It.IsAny<CommandFlags>()))
                        .ReturnsAsync(true);
        redisMock.Setup(r => r.GetDatabase(It.IsAny<int>(), It.IsAny<object>())).Returns(redisDatabaseMock.Object);

        _factory = new WebApplicationFactory<Program>().WithWebHostBuilder(builder =>
        {
            builder.UseContentRoot(contentRoot);
            builder.UseEnvironment("Test");
            builder.ConfigureServices(services =>
            {
                // Remove existing ApplicationDbContext
                var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(ApplicationDbContext));
                if (descriptor != null) services.Remove(descriptor);

                // Add in-memory database
                services.AddDbContext<ApplicationDbContext>(options =>
                    options.UseInMemoryDatabase($"TestDb_{Guid.NewGuid()}"));

                // Override Kafka and Redis with mocks
                services.AddSingleton(kafkaProducerMock.Object);
                services.AddSingleton(redisMock.Object);
            });
        });
        _client = _factory.CreateClient();
    }

    private string GenerateJwtToken(string userId, string role)
    {
        var claims = new[]
        {
            new Claim("sub", userId),
            new Claim(ClaimTypes.Role, role)
        };
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes("a645a295fc581fbbbcd0729e52b1b813c03e63e5318fe373f01a5160773a89e5609be496d8387c8716062aaab1c0c8e7c6dedcadb37d67eee77fd5537d5d210d83ea643916f52f6847643c2bf74a767865e8ead52eab53825ce1743f74a6c34b74a664f226a95c80eef04aa70f12ea9d38cf23e8a19534b2f5a3dc154ffea88b8f9366372f2d32dc347bff80b28ed35dbd5dd3acc0782a93be7324a8d2d97cf0df0007171bde0fd930ebb5f5df443d302bf5c5bf2c8b77106018a5c392ca75ef973cacb69794289dcecd6a97a994385d6791db6e56ac8372d6036ac65239c8b3e517b2343d5e87ba97e572b3ffef78fc8c0d5349c131acf0d21a33ba317e0636"));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: "test",
            audience: "test",
            claims: claims,
            expires: DateTime.Now.AddHours(1),
            signingCredentials: creds);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    private async Task<Guid> CreateTestUser(string username, string email, string password)
    {
        var address = new AddressC(
            "123 Main St",
            "Anytown",
            "State",
            "12345",
            "Country"
        );
        var command = new CreateUserCommand(
            username,
            email,
            password,
            "Test",
            "User",
            "1234567890",
            new DateTime(1990, 1, 1),
            address
        );
        var content = new StringContent(JsonConvert.SerializeObject(command), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/user", content);
        response.EnsureSuccessStatusCode();
        var result = JsonConvert.DeserializeObject<dynamic>(await response.Content.ReadAsStringAsync());
        return result.UserId;
    }

    [Test]
    public async Task Login_ValidCredentials_ReturnsOk()
    {
        var username = "testuser";
        var password = "password123";
        await CreateTestUser(username, "test@example.com", password);

        var command = new LoginUserCommand(username, password);
        var content = new StringContent(JsonConvert.SerializeObject(command), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/user/login", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task Login_InvalidCredentials_ReturnsUnauthorized()
    {
        var command = new LoginUserCommand("nonexistentuser", "wrongpassword");
        var content = new StringContent(JsonConvert.SerializeObject(command), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/user/login", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.Unauthorized));
    }

    [Test]
    public async Task CreateUser_ValidData_ReturnsOk()
    {
        var address = new AddressC(
            "123 Main St",
            "Anytown",
            "State",
            "12345",
            "Country"
        );
        var command = new CreateUserCommand(
            "newuser",
            "newuser@example.com",
            "NewPass123!",
            "New",
            "User",
            "0987654321",
            new DateTime(1995, 5, 5),
            address
        );
        var content = new StringContent(JsonConvert.SerializeObject(command), Encoding.UTF8, "application/json");
    
        // Set timeout
        using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
        try
        {
            var response = await _client.PostAsync("/api/user", content, cts.Token);
            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
            var result = JsonConvert.DeserializeObject<dynamic>(await response.Content.ReadAsStringAsync());
            Assert.That(result.UserId, Is.Not.Null);
        }
        catch (TaskCanceledException)
        {
            Assert.Fail("The HTTP request timed out after 10 seconds.");
        }
    }

    [Test]
    public async Task ForgotPassword_ValidEmail_ReturnsOkAndCreatesToken()
    {
        var email = "dang.nh.909@aptechlearning.edu.vn";
        var userId = await CreateTestUser("testuser", email, "password123");

        var request = new ForgotPasswordRequest { Email = email };
        var content = new StringContent(JsonConvert.SerializeObject(request), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/user/forgot", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));

        using (var scope = _factory.Services.CreateScope())
        {
            var dbContext = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
            var resetToken = await dbContext.PasswordResetTokens
                .FirstOrDefaultAsync(t => t.UserId == userId && !t.IsUsed);
            Assert.That(resetToken, Is.Not.Null);
            Assert.That(resetToken.IsUsed, Is.False);
            Assert.That(resetToken.Expiry, Is.GreaterThan(DateTime.UtcNow));
            Console.WriteLine($"Reset Token: {resetToken.Token}");
        }
    }

    [Test]
    public async Task UpdateUser_ValidData_ReturnsOk()
    {
        var userId = await CreateTestUser("olduser", "old@example.com", "oldpass123");
        var token = GenerateJwtToken(userId.ToString(), "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var updatedAddress = new AddressC(
            "123 Main St",
            "Anytown",
            "State",
            "12345",
            "Country"
        );
        var command = new UpdateUserCommand(
            userId,
            "John",
            "Doe",
            "123-456-7890",
            updatedAddress,
            KycStatus.Pending,
            "Passport",
            "/path/to/document.pdf"
        );
        var content = new StringContent(JsonConvert.SerializeObject(command), Encoding.UTF8, "application/json");
        var response = await _client.PutAsync("/api/user", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task ResetPassword_ValidToken_ReturnsOk()
    {
        var email = "test@example.com";
        var token = "valid-token";
        var request = new ResetPasswordRequest { Email = email, Token = token, NewPassword = "NewPass123" };
        var content = new StringContent(JsonConvert.SerializeObject(request), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/user/reset", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task SubmitKycDocument_ValidData_ReturnsOk()
    {
        var token = GenerateJwtToken(Guid.NewGuid().ToString(), "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var content = new MultipartFormDataContent
        {
            { new StringContent("Passport"), "DocumentType" },
            { new StringContent("123456"), "DocumentNumber" },
            { new StringContent("US"), "IssuingCountry" },
            { new StringContent("2025-01-01"), "ExpiryDate" },
            { new ByteArrayContent(Encoding.UTF8.GetBytes("fake-file-content")), "DocumentFile", "test.pdf" }
        };
        var response = await _client.PostAsync("/api/user/kyc/document", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task GetMyKycDocuments_Authenticated_ReturnsOk()
    {
        var userId = Guid.NewGuid().ToString();
        var token = GenerateJwtToken(userId, "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var response = await _client.GetAsync("/api/user/kyc/documents");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task GetKycDocument_AsOwner_ReturnsOk()
    {
        var userId = Guid.NewGuid().ToString();
        var token = GenerateJwtToken(userId, "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var documentId = Guid.NewGuid();
        var response = await _client.GetAsync($"/api/user/kyc/documents/{documentId}");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task VerifyKycDocument_AsAdmin_ReturnsOk()
    {
        var token = GenerateJwtToken(Guid.NewGuid().ToString(), "Admin");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var documentId = Guid.NewGuid();
        var verification = new KycVerificationDto { Status = "Verified", VerifierNotes = "Looks good" };
        var content = new StringContent(JsonConvert.SerializeObject(verification), Encoding.UTF8, "application/json");
        var response = await _client.PostAsync($"/api/user/kyc/documents/{documentId}/verify", content);

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task GetUserById_ExistingUser_ReturnsOk()
    {
        var userId = Guid.NewGuid();
        var token = GenerateJwtToken(userId.ToString(), "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var response = await _client.GetAsync($"/api/user/{userId}");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task GetUserByEmail_ExistingEmail_ReturnsOk()
    {
        var token = GenerateJwtToken(Guid.NewGuid().ToString(), "User");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var response = await _client.GetAsync("/api/user/email/test@example.com");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task GetAllUsers_ReturnsOk()
    {
        var token = GenerateJwtToken(Guid.NewGuid().ToString(), "Admin");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var response = await _client.GetAsync("/api/user");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task DeleteUser_ExistingUser_ReturnsOk()
    {
        var userId = Guid.NewGuid();
        var token = GenerateJwtToken(userId.ToString(), "Admin");
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        var response = await _client.DeleteAsync($"/api/user/{userId}");

        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }
}