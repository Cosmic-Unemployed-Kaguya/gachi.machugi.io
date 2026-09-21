package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.user.*;
import kaguya.user.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.ResetPasswordReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileRes;
import kaguya.user.domain.user.model.enums.Role;
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

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        MyPageRes resData = userService.getMyPage(idx);

        MyPageResponse response = MyPageResponse.newBuilder()
//                .setUsername(resData.username())
                .setEmail(resData.email())
                .setNickname(resData.nickname())
                .setJoinDate(resData.joinDate().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProfile(
            Empty request,
            StreamObserver<ProfileResponse> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        ProfileRes resData = userService.getProfile(idx);

        ProfileResponse response = ProfileResponse.newBuilder()
                .setName(resData.name())
                .setBirth(resData.birth().toString())
                .setPhone(resData.phone())
                .setGender(resData.gender())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePassword(
            UpdatePasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
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

        userService.updatePassword(idx, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateNickname(
            UpdateNicknameRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        userService.updateNickname(idx, request.getNickname());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(
            Empty request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        userService.withdraw(idx);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

/*
    @Override
    public void findUsername (
            FindUsernameRequest request,
            StreamObserver<FindUsernameResponse> responseObserver
    ) {

        String oneTimeAuthCode = request.getOneTimeAuthCode();

        FindUsernameRes resData = userService.findUsername(oneTimeAuthCode);
        FindUsernameResponse response = FindUsernameResponse.newBuilder()
                .setMaskedUsername(resData.maskedUsername())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
 */

    @Override
    public void resetPassword (
            ResetPasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        ResetPasswordReq reqData = new ResetPasswordReq(
                request.getOneTimeAuthCode(),
                request.getNewPassword()
        );

        userService.resetPassword(reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}