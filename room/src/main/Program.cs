using Chat.Service.Proto;
using Room.Repository;
using Room.Service;
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
//레디스는 연결비용이 좀 있어서 싱글톤으로 연결
builder.Services.AddSingleton<IConnectionMultiplexer>(redis);
//커스텀 리포지토리
builder.Services.AddScoped<RoomRedis>();
//커스텀 서비스
builder.Services.AddScoped<RoomService, RoomServiceImpl>();

//Chat 서비스가 떠 있는 주소(http://localhost:5062)를 적어줌
builder.Services.AddGrpcClient<ChatGrpcManager.ChatGrpcManagerClient>(options =>
{
    //appsettings.json 등에서 읽어오도록 구성하면 더 깔끔함, 나중에 이관
    options.Address = new Uri(builder.Configuration["Services:ChatServerUrl"] ?? "http://localhost:5063");
});

builder.Services.AddControllers();


var app = builder.Build();
//CORS, 이게 없으면 차단함
app.UseCors("AllowAll");
app.MapGrpcService<RoomGrpcService>();
//Https설정
//app.UseHttpsRedirection();

app.MapControllers();

app.Run();

public partial class Program { }