using System.ComponentModel.DataAnnotations;

namespace Room.Model.Dto.Request;

public record UpdateSetRequest(
    [Required]
    long playerIdx
)
{ }