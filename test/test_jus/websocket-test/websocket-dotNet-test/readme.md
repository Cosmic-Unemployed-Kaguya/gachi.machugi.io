# 웹소켓 SignalR/Borker 적용 및 퀴즈 동작 테스트

### 설치해야 할 패키지 (명령어)
- SignalR용 Redis Backplane 패키지: dotnet add package Microsoft.AspNetCore.SignalR.StackExchangeRedis
- 분산 캐시용 Redis 패키지: dotnet add package Microsoft.Extensions.Caching.StackExchangeRedis
- 참고) .net 잘 몰라서 git pull 하면 알아서 설치 되는지 컴퓨터마다 따로 설치해야하느지는 몰?루

## 테스트 방법
1. Redis 띄우기 (Docker로 하던, 로컬환경에서 띄우던 6379 포트로 Redis가 띄워져 있으면 됨)
- 만약 Redis 동작되는거 모니터링 하고 싶다면 (Broker가 동기화 되는 과정을 보고 싶다면)
- redis-cli
- MONITOR
- 위 2개 명령어 입력

2. 서버 2개 띄우기 (port 5062, 5063)
- 채팅 서버에 유자가 몰려서 서버가 로드밸런싱 되었다고 가정
- dotnet run --urls "http://localhost:5062" / dotnet run --urls "http://localhost:5063"

3. 프론트 띄우기 (Live Server)
- connect_server1.html, connect_server2.html을 VsCode 플러그인 Live Server로 프론트 띄우기
- connect_server1.html는 5062 서버로, connect_server2.html는 5063 서버로 연결됨

4. 닉네임 세팅 후 방 입장

5. 채팅 치는거 서버끼리 동기화 되어서 메시지가 올바르게 띄워지는지 확인 (Borker 테스트)

6. Postman 열어서 아래의 구조로 API 쏘기
```
POST http://localhost:5062/quiz/list
{
    "roomId": "room_1",
    "answerList": [
        "사과",
        "바나나",
        "호랑이",
        "컴퓨터"
    ]
}
```

7. connect_server2.html (5063 서버) 유저가 '사과', '바나나' ... 라고 채팅 입력
- 아까 6번 과정에서 5062 으로 정답을 보냈었음
- 5063 서버에 정답을 입력했을 때 정상적으로 정답처리가 되는지 확인 (정답 처리 및 Broker 테스트)
- 유저별 스코어 잘 기록되는지도 확인