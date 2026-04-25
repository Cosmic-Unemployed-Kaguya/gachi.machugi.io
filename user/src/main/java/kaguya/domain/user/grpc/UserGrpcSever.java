package kaguya.domain.user.grpc;

import io.grpc.stub.StreamObserver;
import kaguya.grpc.user.TestRequest;
import kaguya.grpc.user.TestResponse;
import kaguya.grpc.user.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcSever extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void restTest(TestRequest request, StreamObserver<TestResponse> responseObserver) {
        log.info("======= [Envoy REST -> gRPC 테스트] =======");
        log.info("전체 요청 데이터: {}", request);

        TestResponse response = TestResponse.newBuilder()
                .setStatus("SUCCESS")
                .setMessage("REST -> gRPC 연동 성공")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}