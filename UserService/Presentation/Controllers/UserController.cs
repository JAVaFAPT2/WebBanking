
using Application.CQRS.Commands;
using Application.CQRS.Queries;
using Domain.Interface;
using Domain.models;
using Infrastructure.Persistence.Service;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;
using Presentation.Dto;


namespace Presentation.Controllers;


[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "User,Admin")]
public class UserController : ControllerBase
{
    private readonly IMediator _mediator;

    public UserController(IMediator mediator)
    {
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginUserCommand command)
    {
        if (string.IsNullOrWhiteSpace(command.Username) || string.IsNullOrWhiteSpace(command.Password))
        {
            return BadRequest("Username and password are required.");
        }

        try
        {
            var response = await _mediator.Send(command);
            return Ok(response);
        }
        catch (UnauthorizedAccessException)
        {
            return Unauthorized("Invalid username or password.");
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"An error occurred: {ex.Message}");
        }
    }



    [HttpPost]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserCommand command)
    {
        var userId = await _mediator.Send(command);
        return Ok(new { UserId = userId });
    }
  // Add these methods to your existing UserController class

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

        // Convert the uploaded file to byte array and store it
        string documentPath = null;
        if (submission.DocumentFile != null && submission.DocumentFile.Length > 0)
        {
            // In a real implementation, you would:
            // 1. Validate the file (size, type, etc.)
            // 2. Scan for malware
            // 3. Store in secure storage (e.g., Azure Blob, S3)
            // 4. Store the reference path

            // For this example, we'll just generate a path
            string fileName = $"{userId}_{submission.DocumentType}_{DateTime.UtcNow:yyyyMMddHHmmss}{Path.GetExtension(submission.DocumentFile.FileName)}";
            documentPath = $"/secure-storage/kyc-documents/{fileName}";

            // Here you would use your file storage service
            // Example: await _fileStorageService.UploadAsync(submission.DocumentFile, documentPath);
        }

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
}