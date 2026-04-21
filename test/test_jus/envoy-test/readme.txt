테스트 하는 법

1. 최상위 폴더(envoy-test) 에서 아래의 명령어 실행 (protoc 명령 사용하려면 Protocol Buffers Compiler 설치 되어있어야 함)
protoc -I ./envoy-user/src/main/proto -I ./envoy-quiz/src/main/proto -I ./envoy-board/src/main/proto -I ./third_party --include_imports --include_source_info --descriptor_set_out=./infra/envoy/descriptor.pb ./envoy-user/src/main/proto/user.proto ./envoy-quiz/src/main/proto/quiz.proto ./envoy-board/src/main/proto/board.proto
( ./infra/envoy/descriptor.pb 생성 잘 되었는지 확인 )

2. infra 폴더 가서 docker compose up -d 
 
3. 자바 서버 키기 (user/quiz/board)
- 서버 실행 전에 grpc 빌드 (./gradlew generateProto) 

4. postman으로 테스트 
(공통) Headers에 key = Host, value = {도메인 이름}.example.com 세팅. (ex. Host : user.example.com)
(User 서비스) GET http://localhost:8080/user/100
(Quiz 서비스) GET http://localhost:8080/quiz/100
(Board 서비스) GET http://localhost:8080/board/100
-> Json 형식으로 잘 응답오면 테스트 성공

