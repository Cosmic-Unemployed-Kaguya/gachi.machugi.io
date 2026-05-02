using Room.Repository;
using Room.Service;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

//redis 설정 일단 appsettings에서 설정 불러오기
var redisSection = builder.Configuration.GetSection("Redis:ConnectionString").Value;
//설정했는지 확인
if (string.IsNullOrEmpty(redisSection))
{
    throw new Exception("you idiot, redis setting is null or empty.");
}
var redis = ConnectionMultiplexer.Connect($"{redisSection},abortConnect=false");
//레디스는 연결비용이 좀 있어서 싱글톤으로 연결
builder.Services.AddSingleton<IConnectionMultiplexer>(redis);
//커스텀 리포지토리
builder.Services.AddScoped<RoomRedis>();
//커스텀 서비스
builder.Services.AddScoped<RoomService, RoomServiceImpl>();

builder.Services.AddControllers();


var app = builder.Build();

app.UseHttpsRedirection();

app.MapControllers();

app.Run();

public partial class Program { }