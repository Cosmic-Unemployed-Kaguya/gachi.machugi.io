using Chat.ChatControllers;
using Chat.Service;
var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<ChatService>();
builder.Services.AddSignalR(); //SignalR 서비스를 컨테이너에 추가
builder.Services.AddControllers();
//로컬 테스트 시 프론트엔드(HTML 파일 등)에서 접근할 수 있도록 CORS 허용 설정
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.WithOrigins("http://localhost:5062", "http://127.0.0.1:5062") //서버 포트
              .AllowAnyHeader()//모든 헤더 허용
              .AllowAnyMethod()//모든 http 메서드 허용
              .AllowCredentials() //소켓 연결 시 쿠키/인증 세션을 태우기 위한 SignalR 필수 설정 이라고 하는데 솔직히 잘 모름 샤갈
              .SetIsOriginAllowed(_ => true); //전 세계 모든 출처(Origin)의 접속을 보안 검사 없이 무조건 허용하겠다는 뜻 나중에 삭제해야 함
    });
});
var app = builder.Build();

app.UseRouting();//라우팅 켠다는 소리
app.UseCors();//이거 안켜면 .net이 다 거부함
app.MapControllers();
app.MapGet("/", () => "Hello World!");
app.MapHub<ChatHub>("/chat");
app.Run();
