package kaguya.user.domain.user.grpc.interceptor.validation;

import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcValidationConfig {

    @GrpcGlobalServerInterceptor
    public ServerInterceptor customValidationInterceptor() {
        return new CustomValidationInterceptor();
    }
}