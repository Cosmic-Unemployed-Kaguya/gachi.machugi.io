package kaguya.user.domain.user.grpc.interceptor.validation;

import com.google.rpc.ErrorInfo;
import com.google.rpc.Status;
import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.grpc.*;
import io.grpc.protobuf.StatusProto;
import kaguya.user.global.exception.ErrorCode;

/**
 * gRPC 서비스 로직 진입 전 .proto의 validator 작업을 수행하는 인터셉터
 */
public class CustomValidationInterceptor implements ServerInterceptor {

    // .proto 파일을 빌드할 때 생성된 Validator를 찾아주는 인덱스
    private final ReflectiveValidatorIndex index = new ReflectiveValidatorIndex();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        // 클라이언트가 보낸 요청 데이터(Message)를 가로채기 위해 리스너를 랩핑(Wrapping) 후 반환 (익명 클래스)
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    // 역직렬화된 Request Message에 매핑된 Validator를 조회하여 유효성 검사 수행
                    index.validatorFor(message).assertValid(message);
                    // Validation 통과 시 다음 파이프라인(비즈니스 로직)으로 메시지 전달
                    super.onMessage(message);

                } catch (ValidationException e) {
                    // ValidationException 발생 시 표준 규격(ErrorInfo)으로 응답 객체 조립

                    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;  // 400 Bad Request

                    // ErrorInfo 객체 생성
                    ErrorInfo errorInfo = ErrorInfo.newBuilder()
                            .setReason(errorCode.getCode())  // 에러 코드 세팅
                            .setDomain("kaguya.user")  // 도메인(서버명) 세팅
                            .build();

                    Status status = Status.newBuilder()
                            .setCode(io.grpc.Status.Code.INVALID_ARGUMENT.value())  // gRPC 기본 상태 코드 (0~16)
                            .setMessage("입력값이 올바르지 않습니다.")  // 에러 메세지
                            .addDetails(com.google.protobuf.Any.pack(errorInfo))  // ErrorInfo 객체
                            .build();

                    StatusRuntimeException exception = StatusProto.toStatusRuntimeException(status);

                    // 파이프라인 진행을 종료하고 StatusRuntimeException 상태를 즉시 반환
                    call.close(exception.getStatus(), exception.getTrailers());
                }
            }
        };
    }
}
