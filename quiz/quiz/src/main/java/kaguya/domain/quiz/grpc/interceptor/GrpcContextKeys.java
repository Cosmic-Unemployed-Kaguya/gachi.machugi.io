package kaguya.domain.quiz.grpc.interceptor;

import io.grpc.Context;
import io.grpc.Metadata;

public final class GrpcContextKeys {

    private GrpcContextKeys() {
    }

    public static final Metadata.Key<String> USER_IDX_META_KEY =
            Metadata.Key.of("x-user-idx", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> USER_ROLE_META_KEY =
            Metadata.Key.of("x-user-role", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<Long> USER_IDX_CTX_KEY =
            Context.key("userIdx");

    public static final Context.Key<String> USER_ROLE_CTX_KEY =
            Context.key("userRole");
}