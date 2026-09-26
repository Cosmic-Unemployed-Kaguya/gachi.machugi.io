package kaguya.user.domain.verification.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.verification.CheckVerificationCodeRequest;
import kaguya.grpc.verification.CheckVerificationCodeResponse;
import kaguya.grpc.verification.SendVerificationCodeRequest;
import kaguya.grpc.verification.VerificationServiceGrpc;
import kaguya.user.domain.verification.model.dto.request.CheckVerificationCodeReq;
import kaguya.user.domain.verification.model.dto.request.SendVerificationCodeReq;
import kaguya.user.domain.verification.model.dto.response.CheckVerificationCodeRes;
import kaguya.user.domain.verification.model.enums.VerificationType;
import kaguya.user.domain.verification.service.VerificationService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class VerificationGrpcServer extends VerificationServiceGrpc.VerificationServiceImplBase {

    private final VerificationService verificationService;

    @Override
    public void sendVerificationCode(
            SendVerificationCodeRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        // String -> Enum
        VerificationType verificationType = VerificationType.from(request.getVerificationType());
        if (verificationType == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SendVerificationCodeReq reqData = new SendVerificationCodeReq(
                verificationType,
                request.getEmail()
        );

        verificationService.sendVerificationCode(reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void checkVerificationCode(
            CheckVerificationCodeRequest request,
            StreamObserver<CheckVerificationCodeResponse> responseObserver
    ) {

        // String -> Enum
        VerificationType verificationType = VerificationType.from(request.getVerificationType());
        if (verificationType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        CheckVerificationCodeReq reqData = new CheckVerificationCodeReq(
                verificationType,
                request.getVerificationCode(),
                request.getEmail()
        );

        CheckVerificationCodeRes resData = verificationService.checkVerificationCode(reqData);

        CheckVerificationCodeResponse response = CheckVerificationCodeResponse.newBuilder()
                        .setOneTimeAuthCode(resData.oneTimeAuthCode())
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}