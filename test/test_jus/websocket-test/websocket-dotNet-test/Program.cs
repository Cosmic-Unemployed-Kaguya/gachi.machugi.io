using ChatService;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

// 서비스 컨테이너 등록
builder.Services.AddControllers();  // Controllers

builder.Services.AddSignalR()
        // SignalR Backplane을 위한 메시지 브로커(Redis:6379) 설정
        .AddStackExchangeRedis("localhost:6379");

var redisConnectionString = "localhost:6379,abortConnect=false";
var redis = ConnectionMultiplexer.Connect(redisConnectionString);
builder.Services.AddSingleton<IConnectionMultiplexer>(redis);

builder.Services.AddScoped<QuizRedis>();   // DB 접근 전담 리포지토리
builder.Services.AddScoped<QuizService>(); // 퀴즈 비즈니스 로직 전담 서비스

builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.WithOrigins(
                "http://localhost:5062", "http://127.0.0.1:5062",
                "http://localhost:5500", "http://127.0.0.1:5500"  // VsCode Live Server 포트
        )
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials();
    });
});
var app = builder.Build();

app.UseCors();
app.MapControllers();
app.MapHub<QuizHub>("/quiz");

app.MapGet("/", () => "Hello World!");
app.Run();
