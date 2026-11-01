package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.admin.*;
import kaguya.user.domain.user.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.request.BlockSearchReq;
import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.SearchType;
import kaguya.user.domain.user.model.enums.Status;
import kaguya.user.domain.user.service.AdminService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@GrpcService
@RequiredArgsConstructor
public class AdminGrpcServer extends AdminServiceGrpc.AdminServiceImplBase {

    // @PageableDefault, max-page-size 역할
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // 정렬 허용 필드의 화이트리스트
    private static final Map<String, String> USER_SORTABLE_FIELDS = Map.of(
            "join_date", "joinDate",
            "last_login_date", "lastLoginDate"
    );
    private static final Map<String, String> BLOCK_SORTABLE_FIELDS = Map.of(
            "start_date", "startDate",
            "end_date", "endDate"
    );

    // order_by를 보내지 않았을 때 쓰는 기본 정렬
    private static final List<Sort.Order> USER_DEFAULT_ORDERS = List.of(Sort.Order.desc("joinDate"));
    private static final List<Sort.Order> BLOCK_DEFAULT_ORDERS = List.of(Sort.Order.desc("startDate"));

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

        UserSearchReq condition = toSearchReq(request.getCondition());
        Pageable pageable = toPageable(request.getPageParam(), request.getOrderBy(), USER_SORTABLE_FIELDS, USER_DEFAULT_ORDERS);

        GetUsersInfoRes resData = adminService.getUserList(condition, pageable);

        GetUsersInfoResponse response = GetUsersInfoResponse.newBuilder()
                .addAllUserList(resData.userList().stream()
                        .map(this::toUserDetailsResponse)
                        .toList())
                .setPage(resData.page())
                .setSize(resData.size())
                .setTotalElements(resData.totalElements())
                .setTotalPages(resData.totalPages())
                .setHasNext(resData.hasNext())
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

        GetUserDetailsResponse response = toUserDetailsResponse(resData);

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

        BlockSearchReq condition = toBlockSearchReq(request.getCondition());
        Pageable pageable = toPageable(request.getPageParam(), request.getOrderBy(), BLOCK_SORTABLE_FIELDS, BLOCK_DEFAULT_ORDERS);

        GetBlocksInfoRes resData = adminService.getBlockList(condition, pageable);

        List<GetBlockDetailsResponse> protoBlockList = resData.blockList().stream()
                .map(blockDto -> GetBlockDetailsResponse.newBuilder()
                        .setIdx(blockDto.idx())
                        .setUserIdx(blockDto.userIdx())
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
                .setPage(resData.page())
                .setSize(resData.size())
                .setTotalElements(resData.totalElements())
                .setTotalPages(resData.totalPages())
                .setHasNext(resData.hasNext())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * 헬퍼 메서드
     */
    // UserSearchRequest -> UserSearchReq
    private UserSearchReq toSearchReq(UserSearchRequest c) {

        SearchType searchType = switch (c.getSearchType()) {
            case EMAIL -> SearchType.EMAIL;
            case NICKNAME -> SearchType.NICKNAME;
            case SEARCH_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        Role userRole = switch (c.getRole()) {
            case USER -> Role.USER;
            case ADMIN -> Role.ADMIN;
            case ROLE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        Status status = switch (c.getStatus()) {
            case ACTIVE -> Status.ACTIVE;
            case DORMANT -> Status.DORMANT;
            case SUSPENDED -> Status.SUSPENDED;
            case BANNED -> Status.BANNED;
            case WITHDRAWAL -> Status.WITHDRAWAL;
            case STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        String keyword = c.getKeyword().isBlank() ? null : c.getKeyword().trim();

        return new UserSearchReq(searchType, keyword, userRole, status);
    }

    // BlockSearchRequest -> BlockSearchReq
    private BlockSearchReq toBlockSearchReq(BlockSearchRequest c) {

        SearchType searchType = switch (c.getSearchType()) {
            case EMAIL -> SearchType.EMAIL;
            case NICKNAME -> SearchType.NICKNAME;
            case SEARCH_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        ManagementType managementType = switch (c.getManagementType()) {
            case WARNING -> ManagementType.WARNING;
            case TEMP_BAN -> ManagementType.TEMP_BAN;
            case PERM_BAN -> ManagementType.PERM_BAN;
            case MANAGEMENT_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        String keyword = c.getKeyword().isBlank() ? null : c.getKeyword().trim();

        return new BlockSearchReq(searchType, keyword, managementType);
    }

    // PageParam + OrderBy -> Pageable
    private Pageable toPageable(
            PageParam p, String orderBy,
            Map<String, String> sortableFields, List<Sort.Order> defaultOrders
    ) {

        int page = Math.max(p.getPage(), 0);
        int size = p.getSize() <= 0 ? DEFAULT_PAGE_SIZE : Math.min(p.getSize(), MAX_PAGE_SIZE);
        Sort sort = toSort(orderBy, sortableFields, defaultOrders);

        return PageRequest.of(page, size, sort);
    }

    private Sort toSort(String orderBy, Map<String, String> sortableFields, List<Sort.Order> defaultOrders) {

        List<Sort.Order> orders = orderBy.isBlank() ? defaultOrders : parseOrderBy(orderBy, sortableFields);

        return Sort.by(orders);
    }

    // 정렬 항목 검사 (화이트리스트 기준)
    private static List<Sort.Order> parseOrderBy(String orderBy, Map<String, String> sortableFields) {

        List<Sort.Order> orders = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String clause : orderBy.split(",")) {
            String[] tokens = clause.trim().split("\\s+");
            String field = tokens[0];
            boolean desc = tokens.length > 1 && tokens[1].equals("desc");

            String property = sortableFields.get(field);

            if (property == null) {
                throw new BusinessException(ErrorCode.INVALID_SORT_FIELD);
            }
            if (!seen.add(field)) {
                throw new BusinessException(ErrorCode.DUPLICATE_SORT_FIELD);
            }

            Sort.Direction direction = desc ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property, Sort.NullHandling.NULLS_LAST));
        }

        return orders;
    }

    // GetUserDetailsRes → proto
    private GetUserDetailsResponse toUserDetailsResponse(GetUserDetailsRes dto) {
        return GetUserDetailsResponse.newBuilder()
                .setIdx(dto.idx())
                .setEmail(dto.email())
                .setNickname(dto.nickname())
                .setPoint(dto.point())
                .setRole(dto.role())
                .setStatus(dto.status())
                .setJoinDate(dto.joinDate().toString())
                .setLastLoginDate(dto.lastLoginDate() != null ? dto.lastLoginDate().toString() : "")  // null -> 빈 문자열
                .setWithdrawalDate(dto.withdrawalDate() != null ? dto.withdrawalDate().toString() : "")
                .build();
    }
}