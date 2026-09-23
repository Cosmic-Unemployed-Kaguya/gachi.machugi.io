package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.admin.*;
import kaguya.user.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.service.AdminService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class AdminGrpcServer extends AdminServiceGrpc.AdminServiceImplBase {


    private final AdminService adminService;

    @Override
    public void getUserList(
            GetUsersInfoRequest request,
            StreamObserver<GetUsersInfoResponse> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || idx == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetUsersInfoRes resData = adminService.getUserList();

        // todo. 페이징 처리
        List<GetUserDetailsResponse> protoUserList = resData.userList().stream()
                .map(userDto -> GetUserDetailsResponse.newBuilder()
                        .setIdx(userDto.idx())
                        .setEmail(userDto.email())
                        .setNickname(userDto.nickname())
                        .setPoint(userDto.point())
                        .setRole(userDto.role())
                        .setStatus(userDto.status())
                        .setJoinDate(userDto.joinDate().toString())
                        .setLastLoginDate(userDto.lastLoginDate().toString())
                        .setWithdrawalDate(userDto.withdrawalDate().toString())
                        .build()
                )
                .toList();

        GetUsersInfoResponse response = GetUsersInfoResponse.newBuilder()
                .addAllUserList(protoUserList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserDetails(
            GetUserDetailsRequest request,
            StreamObserver<GetUserDetailsResponse> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetUserDetailsRes resData = adminService.getUserDetails(request.getUserIdx());

        GetUserDetailsResponse response = GetUserDetailsResponse.newBuilder()
                .setIdx(resData.idx())
                .setEmail(resData.email())
                .setNickname(resData.nickname())
                .setPoint(resData.point())
                .setRole(resData.role())
                .setStatus(resData.status())
                .setJoinDate(resData.joinDate().toString())
                .setLastLoginDate(resData.lastLoginDate().toString())
                .setWithdrawalDate(resData.withdrawalDate().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateNickname(
            UpdateNicknameRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        adminService.updateNickname(request.getUserIdx(), request.getNickname());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateRole(
            UpdateRoleRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        Role userRole = Role.valueOf(request.getRole().name());
        adminService.updateRole(request.getUserIdx(), userRole);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void blockUser(
            BlockRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long adminIdx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || adminIdx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        ManagementType managementType = ManagementType.valueOf(request.getManagementType().name());
        BlockReq reqData = new BlockReq(
                managementType,
                request.getReason(),
                LocalDateTime.parse(request.getEndDate())
        );

        adminService.blockUser(request.getUserIdx(), adminIdx, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}