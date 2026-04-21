package test.envoyuser;

import com.example.grpc.user.GetUserRequest;
import com.example.grpc.user.GetUserResponse;
import com.example.grpc.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Postman으로
 * GET http://localhost:8080/user/123
 * header 설정: key(Host):value(user.example.com)
 *
 * Json 형식으로 return 잘 오면 성공
 */

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        GetUserResponse response = GetUserResponse.newBuilder()
                .setUserId(request.getUserId())
                .setName("테스트 유저")
                .setEmail("test@user.com")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}