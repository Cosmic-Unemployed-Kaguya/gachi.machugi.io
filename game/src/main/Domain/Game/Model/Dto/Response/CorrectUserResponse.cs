

namespace Game.Model.Dto.Response;

public record CorrectUserResponse
(
    string msg,
    string userNickname,
    long userIdx

)
{ }