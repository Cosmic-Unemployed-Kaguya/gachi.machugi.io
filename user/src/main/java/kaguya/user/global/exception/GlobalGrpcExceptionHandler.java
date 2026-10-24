package kaguya.user.global.exception;

import com.google.rpc.ErrorInfo;
import com.google.rpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.http.HttpStatus;

@Slf4j
@GrpcAdvice  // GrpcService 보고 있다가 예외가 터지면 가로채기
public class GlobalGrpcExceptionHandler {

    // 비즈니스 로직에서 던진 BusinessException 처리
    @GrpcExceptionHandler(BusinessException.class)
    public StatusRuntimeException handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();

        // ErrorInfo 객체 생성
        ErrorInfo errorInfo = ErrorInfo.newBuilder()
                .setReason(errorCode.getCode())  // 에러 코드 세팅
                .setDomain("kaguya.user")  // 도메인(서버명) 세팅
                .build();

        // Status 객체 생성
        Status status = Status.newBuilder()
                .setCode(mapHttpStatusToGrpcStatusCode(errorCode.getStatus()))  // gRPC 기본 상태 코드 (0~16)
                .setMessage(errorCode.getMessage())  // 에러 메세지
                .addDetails(com.google.protobuf.Any.pack(errorInfo))  // ErrorInfo 객체
                .build();

        // gRPC 프레임워크가 인식할 수 있는 런타임 예외로 변환
        return StatusProto.toStatusRuntimeException(status);
    }

    // 그 외 처리하지 못한 모든 예외 처리
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleException(Exception e) {

        // 무슨 문제인지 로그 확인
        log.error("gRPC 서버 내부 에러 발생: ", e);

        Status status = Status.newBuilder()
                .setCode(io.grpc.Status.Code.INTERNAL.value())
                .setMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .build();

        return StatusProto.toStatusRuntimeException(status);
    }

    // HTTP Status -> gRPC Status 변환 메서드
    private int mapHttpStatusToGrpcStatusCode(HttpStatus httpStatus) {
        return switch (httpStatus) {
            case BAD_REQUEST -> io.grpc.Status.Code.INVALID_ARGUMENT.value();  // 400
            case UNAUTHORIZED -> io.grpc.Status.Code.UNAUTHENTICATED.value();  // 401
            case FORBIDDEN -> io.grpc.Status.Code.PERMISSION_DENIED.value();  // 403
            case NOT_FOUND -> io.grpc.Status.Code.NOT_FOUND.value();  // 404
            case CONFLICT -> io.grpc.Status.Code.ALREADY_EXISTS.value();  // 409
            default -> io.grpc.Status.Code.INTERNAL.value();  // 500
        };
    }
}

/*
    gRPC 에러 응답 예시
    {
        "code": 3,
        "message": "입력값이 올바르지 않습니다.",
        "details": [
            {
                "@type": "type.googleapis.com/google.rpc.ErrorInfo",
                "reason": "400_INVALID_INPUT_VALUE",
                "domain": "kaguya.user",
                "metadata": {}
            }
        ]
    }
 */