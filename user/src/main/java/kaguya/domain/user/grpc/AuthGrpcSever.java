package kaguya.domain.user.grpc;

import io.grpc.stub.StreamObserver;
import kaguya.domain.user.model.dto.AccountDTO;
import kaguya.domain.user.model.dto.UserDTO;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.service.AuthService;
import kaguya.domain.user.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcSever extends UserServiceGrpc.UserServiceImplBase {

    private final AuthService authService;
    private final UserMapper userMapper;

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

        try {
            RegisterReq registerReq = userMapper.toRegisterReq(request);
            authService.register(registerReq);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setStatus("201")
                    .setMessage("회원가입 성공")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setStatus("400")
                    .setMessage("회원가입 실패: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkToken(CheckTokenRequest request, StreamObserver<CheckTokenResponse> responseObserver) {

        try {
            String userId = authService.checkToken(request.getAccessToken());

            CheckTokenResponse response = CheckTokenResponse.newBuilder()
                    .setStatus("200")
                    .setMessage("인증 성공")
                    .setId(userId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            CheckTokenResponse response = CheckTokenResponse.newBuilder()
                    .setStatus("401")
                    .setMessage("유효하지 않은 토큰입니다.")
                    .setId("")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}