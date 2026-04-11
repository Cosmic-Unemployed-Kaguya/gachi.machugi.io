using System.ComponentModel.DataAnnotations;

namespace Room.Model.Dto.Request;
//업데이트 할 때 쓸 request
public record UpdateRoomRequest(
    string? name, //방 이름
    int? maxOccupancy, //최대 인원
    long? timeLimit, //시간제한(퀴즈)
    bool? isPublic, //공개
    string? password, //비번
    long? quizIdx, //사용하는 퀴즈의 idx
    long? hostIdx //방장
)
{ }