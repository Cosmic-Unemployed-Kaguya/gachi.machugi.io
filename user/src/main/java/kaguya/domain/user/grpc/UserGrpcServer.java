package kaguya.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.domain.user.model.dto.response.MyPageRes;
import kaguya.domain.user.model.dto.UserDto;
import kaguya.domain.user.service.UserService;
import kaguya.grpc.user.*;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void getMyPage(
            Empty request,
            StreamObserver<MyPageResponse> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        MyPageRes data = userService.getMyPage(username);

        MyPageResponse response = MyPageResponse.newBuilder()
                .setUsername(data.username())
                .setEmail(data.email())
                .setNickname(data.nickname())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProfile(
            Empty request,
            StreamObserver<ProfileResponse> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        UserDto data = userService.getProfile(username);

        ProfileResponse response = ProfileResponse.newBuilder()
                .setName(data.name())
                .setBirth(data.birth().toString())
                .setPhone(data.phone())
                .setGender(data.gender())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePasswords(
            UpdatePasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {
        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        UpdatePasswordReq reqData = new UpdatePasswordReq(
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        userService.updatePassword(username, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateNickname(
            UpdateNicknameRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        UpdateNicknameReq reqData = new UpdateNicknameReq(
                request.getNickname()
        );

        userService.updateNickname(username, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }


    @Override
    public void withdraw(
            Empty request,
            StreamObserver<Empty> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        if (username == null) {
            responseObserver.onError(
                    io.grpc.Status.UNAUTHENTICATED
                            .withDescription("로그인이 필요한 서비스입니다.")
                            .asRuntimeException()
            );
            return;
        }

        userService.withdraw(username);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}