

using Common;
using Game.Service;
using Quiz.Service.Proto;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

//Cors설정
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});
//grpc 엔진 추가
builder.Services.AddGrpc().AddJsonTranscoding();
//redis 설정 일단 appsettings에서 설정 불러오기
var redisSection = builder.Configuration.GetSection("Redis:ConnectionString").Value;
//설정했는지 확인
if (string.IsNullOrEmpty(redisSection))
{
    throw new Exception("you idiot, redis setting is null or empty.");
}
//문자열 직접 보간 대신 ConfigurationOptions를 활용해 안전하게 파싱
var redisOptions = ConfigurationOptions.Parse(redisSection);
redisOptions.AbortOnConnectFail = false;// 연결 시도 시 실패해도 앱 셧다운 방지
redisOptions.ConnectTimeout = 5000;// 연결 타임아웃 (ms)
var redis = ConnectionMultiplexer.Connect(redisOptions);

builder.Services.AddScoped<GameService, GameServiceImpl>();

builder.Services.AddSingleton<RedisPub>();
builder.Services.AddSingleton<RedisSub>();

builder.Services.AddGrpcClient<QuizGrpcManager.QuizGrpcManagerClient>(options =>
{
    options.Address = new Uri(builder.Configuration["Services:QuizServerUrl"] ?? "http://localhost:5064");
});

builder.Services.AddControllers();
var app = builder.Build();
app.UseCors("AllowAll");
app.MapGrpcService<GameGrpcService>();
//Https설정
//app.UseHttpsRedirection();

app.MapControllers();

app.Run();

public partial class Program { }