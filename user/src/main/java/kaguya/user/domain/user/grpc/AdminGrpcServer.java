package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.admin.*;
import kaguya.user.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
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
                        .setLastLoginDate(userDto.lastLoginDate() != null ? userDto.lastLoginDate().toString() : "")  // null -> 빈 문자열
                        .setWithdrawalDate(userDto.withdrawalDate() != null ? userDto.withdrawalDate().toString() : "")
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
                .setLastLoginDate(resData.lastLoginDate() != null ? resData.lastLoginDate().toString() : "")
                .setWithdrawalDate(resData.withdrawalDate() != null ? resData.withdrawalDate().toString() : "")
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

        // endDate가 비어있다면 null로 처리 (영구 정지는 종료일 불필요)
        LocalDateTime endDate = request.getEndDate().isBlank() ? null : LocalDateTime.parse(request.getEndDate());
        // Proto Enum(통신 계층) -> Domain Enum(비즈니스 계층) 변환
        ManagementType managementType = ManagementType.valueOf(request.getManagementType().name());
        BlockReq reqData = new BlockReq(
                managementType,
                request.getReason(),
                endDate
        );

        adminService.blockUser(request.getUserIdx(), adminIdx, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getBlockList(
            GetBlockListRequest request,
            StreamObserver<GetBlocksInfoResponse> responseObserver
    ) {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();
        if (!Role.ADMIN.name().equals(role) || idx == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetBlocksInfoRes resData = adminService.getBlockList();

        // todo. 페이징 처리
        List<GetBlockDetailsResponse> protoBlockList = resData.blockList().stream()
                .map(blockDto -> GetBlockDetailsResponse.newBuilder()
                        .setIdx(blockDto.idx())
                        .setUserEmail(blockDto.userEmail())
                        .setUserNickname(blockDto.userNickname())
                        .setManagementType(blockDto.managementType())
                        .setReason(blockDto.reason())
                        .setStartDate(blockDto.startDate().toString())
                        .setEndDate(blockDto.endDate() != null ? blockDto.endDate().toString() : "")
                        .setAdminNickname(blockDto.adminNickname())
                        .build()
                )
                .toList();

        GetBlocksInfoResponse response = GetBlocksInfoResponse.newBuilder()
                .addAllBlockList(protoBlockList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}