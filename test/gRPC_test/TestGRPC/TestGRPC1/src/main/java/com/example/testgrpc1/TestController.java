package com.example.testgrpc1;

import com.example.grpc.hello.HelloReply;
import com.example.grpc.hello.HelloRequest;
import com.example.grpc.hello.HelloServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestClient testClient;

    @GrpcClient("service-b") // yml 내 정의된 이름
    private HelloServiceGrpc.HelloServiceBlockingStub helloStub;

    @GetMapping("/test/grpc")
    public String Test(@RequestParam String name){

        // 1. 요청 생성
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();

        // 2. gRPC 호출
        HelloReply response = helloStub.sayHello(request);

        // 3. 반환
        return  response.getMessage();
    }

    @GetMapping("/test/feign")
    public String Test2(@RequestParam String name){

        // 1. 요청 생성
        String res = testClient.test222(name);

        // 2. 반환
        return res;
    }
}
