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
        String username = headers.get(GrpcContextKeys.USER_ID_META_KEY);
        String role = headers.get(GrpcContextKeys.USER_ROLE_META_KEY);

        // gRPC Context(상태 저장소) 가져오기
        Context context = Context.current();

        // 헤더에 유저 정보가 있다면, 서버 내부에서 사용할 수 있도록 Context에 값 세팅
        if (username != null) {
            context = context.withValue(GrpcContextKeys.USER_ID_CTX_KEY, username)
                    .withValue(GrpcContextKeys.USER_ROLE_CTX_KEY, role);
        }

        // Context를 담아서 다음 Interceptor 또는 실제 gRPC 서비스 로직으로 요청 전달
        return Contexts.interceptCall(context, call, headers, next);
    }
}