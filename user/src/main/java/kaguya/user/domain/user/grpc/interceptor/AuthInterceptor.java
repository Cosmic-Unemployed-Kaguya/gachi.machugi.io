package kaguya.user.domain.user.grpc.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor  // 모든 gRPC 요청에 자동 적용
public class AuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next)
    {

        // HTTP의 Request Header와 같은 gRPC Metadata에서 유저 정보 추출
        String userIdx = headers.get(GrpcContextKeys.USER_IDX_META_KEY);
        String role = headers.get(GrpcContextKeys.USER_ROLE_META_KEY);

        // gRPC Context(상태 저장소) 가져오기
        Context context = Context.current();

        // 헤더에 유저 정보가 있다면, 서버 내부에서 사용할 수 있도록 Context에 값 세팅
        if (userIdx != null && !userIdx.isBlank()) {
            try {
                Long idx = Long.valueOf(userIdx);
                context = context.withValue(GrpcContextKeys.USER_IDX_CTX_KEY, idx)
                        .withValue(GrpcContextKeys.USER_ROLE_CTX_KEY, role);
            } catch (NumberFormatException e) {
                // 데이터 포멧(Long 타입)이 맞지 않을 경우
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid data format"), new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }
        }

        // Context를 담아서 다음 Interceptor 또는 실제 gRPC 서비스 로직으로 요청 전달
        return Contexts.interceptCall(context, call, headers, next);
    }
}