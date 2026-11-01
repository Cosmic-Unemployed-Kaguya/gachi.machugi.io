package kaguya.domain.quiz.grpc.interceptor;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

// 다음 ISSUE 투두 : 사용자 신원 확인을 위한 gRPC 인터셉터
@GrpcGlobalServerInterceptor
public class AuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String userIdx = headers.get(GrpcContextKeys.USER_IDX_META_KEY);
        String role = headers.get(GrpcContextKeys.USER_ROLE_META_KEY);

        Long idx = null;

        if (!"GUEST".equals(role) && userIdx != null && !userIdx.isBlank()) {
            try {
                idx = Long.valueOf(userIdx);

                if (idx <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                call.close(
                        Status.UNAUTHENTICATED.withDescription("Invalid user ID"),
                        new Metadata()
                );
                return new ServerCall.Listener<ReqT>() {};
            }
        }

        Context context = Context.current()
                .withValue(GrpcContextKeys.USER_IDX_CTX_KEY, idx)
                .withValue(GrpcContextKeys.USER_ROLE_CTX_KEY, role);

        return Contexts.interceptCall(context, call, headers, next);
    }
}