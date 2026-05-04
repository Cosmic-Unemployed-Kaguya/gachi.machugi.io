package kaguya.domain.user.grpc;

import io.grpc.stub.StreamObserver;
import kaguya.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.domain.user.model.enums.Role;
import kaguya.grpc.user.TestRequest;
import kaguya.grpc.user.TestResponse;
import kaguya.grpc.user.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    /**
     * REST API 동작 테스트 용 (서비스 로직 X)
     */
    @Override
    public void restTest(
            TestRequest request,
            StreamObserver<TestResponse> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();

        log.info("======= [Envoy REST -> gRPC 테스트] =======");
        log.info("전체 요청 데이터: {}", request);

        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        if (!role.equals(Role.ADMIN.toString())) {
            responseObserver.onError(
                    io.grpc.Status.PERMISSION_DENIED
                            .withDescription("관리자 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        TestResponse response = TestResponse.newBuilder()
                .setStatus("SUCCESS")
                .setMessage("REST -> gRPC 연동 성공: " + username + ", " + role)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}