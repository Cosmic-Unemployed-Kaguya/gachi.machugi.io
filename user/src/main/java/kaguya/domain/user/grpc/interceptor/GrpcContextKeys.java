package kaguya.domain.user.grpc.interceptor;

import io.grpc.Context;
import io.grpc.Metadata;

public class GrpcContextKeys {

    // [외부 -> 내부] gRPC 통신 시 헤더(Metadata)에서 값을 꺼내기 위한 키
    public static final Metadata.Key<String> USER_ID_META_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> USER_ROLE_META_KEY =
            Metadata.Key.of("x-user-role", Metadata.ASCII_STRING_MARSHALLER);

    // [내부 저장소] 서버 내부 비즈니스 로직에서 꺼내 쓰기 위해 Context에 저장할 때 사용하는 키
    public static final Context.Key<String> USER_ID_CTX_KEY = Context.key("userId");
    public static final Context.Key<String> USER_ROLE_CTX_KEY = Context.key("userRole");
}