using System.ComponentModel.DataAnnotations;

namespace Game.Model.Dto.Request;

// 게임 시작 요청
public record GameStartRequest
(
    [Required]
    long roomIdx       // 방 식별자
)
{ }