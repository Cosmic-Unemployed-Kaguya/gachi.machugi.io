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
    [HttpGet("{idx}")]
    public async Task<IActionResult> GetRoomByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindRoom(idx));
    }
    [HttpGet("info/{idx}")]
    public async Task<IActionResult> GetRoomInfoByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindRoomInfo(idx));
    }
    [HttpGet("set/{idx}")]
    public async Task<IActionResult> GetPlayerSetInRoomByIdx(
        [FromRoute] long idx
    )
    {
        return Ok(await _roomService.FindSetInfo(idx));
    }
    [HttpPatch("info/{idx}")]
    public async Task<IActionResult> UpdateRoom(
        [FromRoute] long idx,
        [FromBody] UpdateRoomRequest request
    )
    {
        return Ok(await _roomService.UpdateRoomInfo(idx, request));
    }
    [HttpPost("set/add/{idx}")]
    public async Task<IActionResult> addPlayerToRoomByIdx(
        [FromRoute] long idx,
        [FromBody] UpdateSetRequest request
    )
    {
        return Ok(await _roomService.AddPlayerToRoom(idx, request));
    }
    [HttpPost("set/remove/{idx}")]
    public async Task<IActionResult> RemovePlayerFromRoomByIdx(
        [FromRoute] long idx,
        [FromBody] UpdateSetRequest request
    )
    {
        return Ok(await _roomService.RemovePlayerFromRoom(idx, request));
    }
    [HttpDelete("{idx}")]
    public async Task<IActionResult> DeleteRoomByIdx(
        [FromRoute] long idx
    )
    {
        await _roomService.DeleteRoom(idx);
        return NoContent();
    }
}