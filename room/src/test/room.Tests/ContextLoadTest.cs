using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;

namespace room.Tests;

/*
IClassFixture<T>: 이 테스트 클래스는 T라는 물건을 공유해서 쓸 거라고 xUnit에게 알려주는 인터페이스
WebApplicationFactory : 실제 Program.cs를 실행시켜서 메모리에 가상 웹 서버를 띄워주는 역할(공장). Spring의 @SpringBootTest와 같은 역할
지금 이 파일이 spring의 ContextLoad임 샤갈
*/
public class ContextLoadTest : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public ContextLoadTest(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
    }
    [Fact]
    public void Context_Start_Test()
    {
        //1. 일회용 서비스 범위 생성 -> 나 이 서비스 스코프로 쓸거임이라는 소리
        using var scope = _factory.Services.CreateScope();
        //2. DI 컨테이너(빈 담아두는 그거) 접근
        var service = scope.ServiceProvider;
        //3. 컨테이너가 비어있지 않은지 검증
        Assert.NotNull(service);
    }
    [Fact]
    public async Task Application_Start_Test()
    {
        // 1. 가상 서버에 접속할 HTTP 클라이언트 생성
        var client = _factory.CreateClient();
        // 2. 서버의 루트("/") 경로로 GET 요청 전송("/" 경로는 만든 적은 없는데 어차피 응답은 보내니까 괜찮음)
        var response = await client.GetAsync("/");
        // 3. 응답 객체가 생성되었는지 확인
        Assert.NotNull(response);
    }
}