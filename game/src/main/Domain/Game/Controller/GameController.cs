

using Game.Service;
using Microsoft.AspNetCore.Mvc;

namespace Game.Controller;

[ApiController]
[Route("game")]
public class GameController : ControllerBase
{
    private readonly GameService _gameService;

    public GameController(GameService gameService)
    {
        _gameService = gameService;
    }

    [HttpPost("start/{idx}")]
    public async Task<IActionResult> GameStart(
        [FromRoute] long idx
    )
    {
        return Ok(await _gameService.GameStart(idx));
    }
}