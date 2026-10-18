
namespace Game.Model.Dto.Request;


public record EventRequest<T>
(
    // 변수명을 event를 못쓴다고 하더라고?
    string @event,
    T? data
    
)
{ }