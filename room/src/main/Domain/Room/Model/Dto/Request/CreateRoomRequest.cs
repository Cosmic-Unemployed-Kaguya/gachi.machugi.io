using System.ComponentModel.DataAnnotations;

namespace Room.Model.Dto.Request;

public record CreateRoomRequest
(
    [Required]
    string name,        //이름
    [Required]
    int maxOccupancy,  //인원수
    [Required]
    long timeLimit,          //제한시간
    [Required]
    bool isPublic,     //공개여부
    string? password,   //방 비밀번호
    [Required]
    long quizIdx,      //방에서 사용하는 퀴즈의 idx값
    [Required]
    long hostIdx //만든사람
)
{ }