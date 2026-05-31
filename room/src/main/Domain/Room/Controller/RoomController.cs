using Microsoft.AspNetCore.Mvc;
using Room.Model.Dto.Request;
using Room.Model.Dto.Response;
using Room.Service;

namespace Room.Controller;

[ApiController]
[Route("room")]
public class RoomController : ControllerBase
{
    private readonly RoomService _roomService;

    public RoomController(RoomService roomService)
    {
        _roomService = roomService;
    }
    //방생성
    [HttpPost]
    public async Task<IActionResult> CreateRoom(
        [FromBody] CreateRoomRequest request
    )
    {
        RoomInfoResponse response = await _roomService.CreateRoom(request);
        return CreatedAtAction(
            nameof(GetRoomByIdx),
            new { idx = response.Idx },
            response
        );
    }
    //방 조회 정보
    [HttpGet("{idx}")]
    public async Task<IActionResult> GetRoomByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindRoom(idx));
    }
    //방의 정보만 조회
    [HttpGet("info/{idx}")]
    public async Task<IActionResult> GetRoomInfoByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindRoomInfo(idx));
    }
    //방의 set 조회
    [HttpGet("set/{idx}")]
    public async Task<IActionResult> GetPlayerSetInRoomByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindSetInfo(idx));
    }
    //방 정보 수정
    [HttpPatch("info/{idx}")]
    public async Task<IActionResult> UpdateRoom(
        [FromRoute] long idx,
        [FromBody] UpdateRoomRequest request
    )
    {
        return Ok(await _roomService.UpdateRoomInfo(idx, request));
    }
    //방에 플레이어 추가
    [HttpPost("set/add/{idx}")]
    public async Task<IActionResult> addPlayerToRoomByIdx(
        [FromRoute] long idx,
        [FromBody] UpdateSetRequest request
    )
    {
        return Ok(await _roomService.AddPlayerToRoom(idx, request));
    }
    //방에 플레이어 제거
    [HttpPost("set/remove/{idx}")]
    public async Task<IActionResult> RemovePlayerFromRoomByIdx(
        [FromRoute] long idx,
        [FromBody] UpdateSetRequest request
    )
    {
        return Ok(await _roomService.RemovePlayerFromRoom(idx, request));
    }
    //방 삭제
    [HttpDelete("{idx}")]
    public async Task<IActionResult> DeleteRoomByIdx(
        [FromRoute] long idx
    )
    {
        await _roomService.DeleteRoom(idx);
        return NoContent();
    }
}