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

builder.Services.AddControllers();


var app = builder.Build();

app.UseHttpsRedirection();

app.MapControllers();

app.Run();
