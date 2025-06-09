using Application.CQRS.Commands;
using Application.CQRS.Queries;
using Domain.Interface;
using Domain.models;
using Infrastructure.Persistence.Service;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Presentation.Dto;
using ForgotPasswordRequest = Presentation.Dto.ForgotPasswordRequest;
using ResetPasswordRequest = Presentation.Dto.ResetPasswordRequest;
using Microsoft.Extensions.Logging;
using Application.CQRS.DTO;

namespace Presentation.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "User,Admin")]
public class UserController(IMediator mediator, ILogger<UserController> logger) : ControllerBase
{
    private readonly IMediator _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
    private readonly ILogger<UserController> _logger = logger;

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        _logger.LogInformation("Login request: {@Command}", request);
        if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
        {
            var badReq = new GenericApiResponse<AuthResponse> { Success = false, Message = "Username and password are required.", Data = null };
            _logger.LogWarning("Login failed: missing username or password");
            return BadRequest(badReq);
        }

        try
        {
            var command = new LoginUserCommand(request.Username, request.Password);
            var response = await _mediator.Send(command);
            if (response == null)
            {
                var fail = new GenericApiResponse<AuthResponse> { Success = false, Message = "Invalid username or password.", Data = null };
                _logger.LogWarning("Login failed: invalid credentials for {Username}", request.Username);
                return Unauthorized(fail);
            }
            var ok = new GenericApiResponse<AuthResponse> { Success = true, Data = response };
            _logger.LogInformation("Login success for {Username}", request.Username);
            return Ok(ok);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Login error");
            var err = new GenericApiResponse<AuthResponse> { Success = false, Message = "Internal server error.", Data = null };
            return StatusCode(500, err);
        }
    }

    [HttpPost]
    [AllowAnonymous]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserCommand command)
    {
        _logger.LogInformation("Register request: {@Command}", command);
        if (string.IsNullOrWhiteSpace(command.Username) || string.IsNullOrWhiteSpace(command.Password) || string.IsNullOrWhiteSpace(command.Email))
        {
            var badReq = new GenericApiResponse<AuthResponse> { Success = false, Message = "Username, email, and password are required.", Data = null };
            _logger.LogWarning("Register bad request: {@Response}", badReq);
            return BadRequest(badReq);
        }

        try
        {
            var userId = await _mediator.Send(command);
            // For now, return a dummy token and the username/email
            var auth = new AuthResponse
            {
                Token = "dummy-token",
                Username = command.Username,
                Email = command.Email,
                UserId = userId
            };
            var okResp = new GenericApiResponse<AuthResponse> { Success = true, Data = auth, Message = "Registration successful." };
            _logger.LogInformation("Register success: {@Response}", okResp);
            return Ok(okResp);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Register exception");
            var errResp = new GenericApiResponse<AuthResponse> { Success = false, Message = ex.Message, Data = null };
            return StatusCode(500, errResp);
        }
    }

    /// <summary>
    /// Request a password reset link to be sent to the user's email.
    /// </summary>
    [HttpPost("forgot")]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequest request)
    {
        await _mediator.Send(new ForgotPasswordCommand(request.Email));
        // Always return 200 to prevent email enumeration
        return Ok(new { Message = "If the email exists, a reset link has been sent." });
    }

    /// <summary>
    /// Reset the user's password using the provided token.
    /// </summary>
    [HttpPost("reset")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequest request)
    {
        var result = await _mediator.Send(new ResetPasswordCommand(request.Email, request.Token, request.NewPassword));
        if (!result)
            return BadRequest(new { Message = "Invalid token or email." });

        return Ok(new { Message = "Password has been reset successfully." });
    }

    [HttpPost("kyc/document")]
    [Authorize(Roles = "User")]
    public async Task<IActionResult> SubmitKycDocument([FromForm] KycDocumentSubmissionDto submission)
    {
        try
        {
            // Get user ID from claims (assuming JWT authentication)
            var userIdClaim = User.FindFirst("sub")?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !Guid.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            // Validate the document file
            if (submission.DocumentFile.Length == 0)
            {
                return BadRequest(new { Message = "Document file is required." });
            }

            // Validate file size (e.g., max 10MB)
            if (submission.DocumentFile.Length > 10 * 1024 * 1024)
            {
                return BadRequest(new { Message = "File size exceeds the maximum limit of 10MB." });
            }

            // Validate file type (e.g., only allow PDF, JPG, PNG)
            var allowedExtensions = new[] { ".pdf", ".jpg", ".jpeg", ".png" };
            var fileExtension = Path.GetExtension(submission.DocumentFile.FileName).ToLowerInvariant();
            if (!allowedExtensions.Contains(fileExtension))
            {
                return BadRequest(new { Message = "Only PDF, JPG, and PNG files are allowed." });
            }

            // Generate a unique filename
            string fileName = $"{userId}_{submission.DocumentType}_{DateTime.UtcNow:yyyyMMddHHmmss}{fileExtension}";
            string documentPath = $"/secure-storage/kyc-documents/{fileName}";

            // In a real implementation, you would:
            // 1. Save the file to a secure storage location
            // 2. Use a proper file storage service (Azure Blob Storage, AWS S3, etc.)
            
            // For this example, we'll just store the path reference
            // In a real application, you would need to implement or inject a file storage service
            
            // Example of how you might save the file in a real application:
            //this is not save just a test
            // string uploadsFolder = Path.Combine(_webHostEnvironment.WebRootPath, "secure-storage", "kyc-documents");
            // if (!Directory.Exists(uploadsFolder))
            //     Directory.CreateDirectory(uploadsFolder);
            // string filePath = Path.Combine(uploadsFolder, fileName);
            // using (var fileStream = new FileStream(filePath, FileMode.Create))
            // {
            //     await submission.DocumentFile.CopyToAsync(fileStream);
            // }

            // Create KYC document
            var kycDocument = new KycDocument
            {
                UserId = userId,
                DocumentType = submission.DocumentType,
                DocumentNumber = submission.DocumentNumber,
                IssuingCountry = submission.IssuingCountry,
                ExpiryDate = submission.ExpiryDate,
                DocumentPath = documentPath,
                Status = "Pending",
                SubmissionDate = DateTime.UtcNow
            };

            // Save KYC document
            var command = new SaveKycDocumentCommand(kycDocument);
            var documentId = await _mediator.Send(command);

            return Ok(new { DocumentId = documentId, Message = "KYC document submitted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = $"An error occurred: {ex.Message}" });
        }
    }

    [HttpGet("kyc/documents")]
    [Authorize(Roles = "User")]
    public async Task<IActionResult> GetMyKycDocuments()
    {
        try
        {
            // Get user ID from claims
            var userIdClaim = User.FindFirst("sub")?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !Guid.TryParse(userIdClaim, out var userId))
            {
                return Unauthorized();
            }

            var query = new GetKycDocumentByUserIdQueries(userId);
            var documents = await _mediator.Send(query);

            return Ok(documents);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = $"An error occurred: {ex.Message}" });
        }
    }

    [HttpGet("kyc/documents/{documentId}")]
    [Authorize(Roles = "User,Admin")]
    public async Task<IActionResult> GetKycDocument(Guid documentId)
    {
        try
        {
            var query = new GetKycDocumentByUserIdQueries(documentId);
            var documents = await _mediator.Send(query);
            var document = documents.FirstOrDefault();

            if (document == null)
                return NotFound();

            // Check user permission
            if (User.IsInRole("Admin") || User.FindFirst("sub")?.Value == document.UserId.ToString())
                return Ok(document);

            return Forbid();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = $"An error occurred: {ex.Message}" });
        }
    }

    [HttpPost("kyc/documents/{documentId}/verify")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> VerifyKycDocument(Guid documentId, [FromBody] KycVerificationDto verification)
    {
        try
        {
            var command = new VerifyKycCommand(documentId, verification.Status, verification.VerifierNotes);
            var result = await _mediator.Send(command);

            if (!result)
                return NotFound(new { Message = "Document not found." });

            return Ok(new { Message = "KYC document verification updated successfully." });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = $"An error occurred: {ex.Message}" });
        }
    }

    [HttpGet("{userId}")]
    public async Task<IActionResult> GetUserById(Guid userId)
    {
        var query = new GetUserByIdQuery(userId);
        var result = await _mediator.Send(query);
        return result == null ? NotFound() : Ok(result);
    }

    [HttpGet("email/{email}")]
    public async Task<IActionResult> GetUserByEmail(string email)
    {
        var query = new GetUserByEmailQuery(email);
        var result = await _mediator.Send(query);
        return result == null ? NotFound() : Ok(result);
    }

    [HttpGet]
    public async Task<IActionResult> GetAllUsers()
    {
        var query = new GetAllUsersQuery();
        var result = await _mediator.Send(query);
        return Ok(result);
    }

    [HttpPut]
    public async Task<IActionResult> UpdateUser([FromBody] UpdateUserCommand command)
    {
        await _mediator.Send(command);
        return Ok();
    }

    [HttpDelete("{userId}")]
    public async Task<IActionResult> DeleteUser(Guid userId)
    {
        var command = new DeleteUserCommand(userId);
        await _mediator.Send(command);
        return Ok();
    }

    [HttpGet("health")]
    [AllowAnonymous]
    public IActionResult Health()
    {
        return Ok(new { status = "Healthy" });
    }

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] CreateUserCommand command)
    {
        // Reuse the CreateUser logic
        return await CreateUser(command);
    }

    [HttpGet("/api/health")]
    [AllowAnonymous]
    public IActionResult GlobalHealth()
    {
        return Ok(new { status = "Healthy" });
    }
}