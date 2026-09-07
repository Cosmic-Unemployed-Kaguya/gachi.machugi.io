package kaguya.chat_spring.chat.service;

import kaguya.grpc.user.GetNicknameRequest;
import kaguya.grpc.user.GetNicknameResponse;
import kaguya.grpc.user.UserInternalServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserInternalServiceGrpc.UserInternalServiceBlockingStub userStub;

    public String getNickname(String userId) {

        GetNicknameResponse response = userStub.getNickname(
                GetNicknameRequest.newBuilder()
                        .setUserId(userId)
                        .build()
        );

        return response.getNickname();
    }
}
