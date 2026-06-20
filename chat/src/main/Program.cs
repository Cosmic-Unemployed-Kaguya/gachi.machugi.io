using ChatService;
var builder = WebApplication.CreateBuilder(args);
//SignalR 서비스를 컨테이너에 추가
builder.Services.AddSignalR();

//로컬 테스트 시 프론트엔드(HTML 파일 등)에서 접근할 수 있도록 CORS 허용 설정
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.WithOrigins("http://localhost:5062", "http://127.0.0.1:5062") //서버 포트
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials(); //SignalR 필수 설정 이라고 하는데 솔직히 잘 모름 샤갈
    });
});
var app = builder.Build();

app.UseCors();
app.MapGet("/", () => "Hello World!");
app.MapHub<ChatHub>("/chat");
app.Run();
