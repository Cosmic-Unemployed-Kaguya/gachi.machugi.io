package kaguya.user.domain.user.grpc;

import io.grpc.stub.StreamObserver;
import kaguya.grpc.user.GetNicknameRequest;
import kaguya.grpc.user.GetNicknameResponse;
import kaguya.grpc.user.UserInternalServiceGrpc;
import kaguya.user.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class UserInternalServer extends UserInternalServiceGrpc.UserInternalServiceImplBase {

    private final UserService userService;

    @Override
    public void getNickname(
            GetNicknameRequest request,
            StreamObserver<GetNicknameResponse> responseObserver)
    {

        String username = request.getUserId();

        String nickname = userService.getNicknameByUsername(username);

        GetNicknameResponse response = GetNicknameResponse.newBuilder()
                .setNickname(nickname)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
