package com.example.testgrpc2;
import com.example.grpc.hello.HelloReply;
import com.example.grpc.hello.HelloRequest;
import com.example.grpc.hello.HelloServiceGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

// GRPC 서버로 등록 된다고 함
@GrpcService
public class TestService extends HelloServiceGrpc.HelloServiceImplBase{

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        // 대충 서비스 로직~~
        String podName = System.getenv("HOSTNAME");
        if (podName == null) podName = "Local-PC";
        String message = "안녕!!!!!" + request.getName() +" from:" +podName;

        HelloReply reply = HelloReply.newBuilder()
                                        .setMessage(message)
                                        .build();
        // 응답 전달
        responseObserver.onNext(reply);
        // 통신 종료(스트림 종료)
        responseObserver.onCompleted();

    }
}
