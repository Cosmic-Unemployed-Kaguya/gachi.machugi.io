package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.user.*;
import kaguya.user.domain.common.model.enums.Role;
import kaguya.user.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileReq;
import kaguya.user.domain.user.service.UserService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.regex.Pattern;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    // 정규식 패턴
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$");

    @Override
    public void getMyPage(
            Empty request,
            StreamObserver<MyPageResponse> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || username == null || username.isBlank()) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
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
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        ProfileReq data = userService.getProfile(username);

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
    public void updatePassword(
            UpdatePasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        String username = GrpcContextKeys.USER_ID_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        String newPassword = request.getNewPassword();
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
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
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
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
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        userService.withdraw(username);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}