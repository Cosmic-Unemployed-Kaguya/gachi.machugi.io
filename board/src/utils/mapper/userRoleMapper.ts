
import { UserRole as GrpcRole } from "../../generated/user";
import { UserRole as AppRole } from "../../model/enum/userRole";


// 앱 내에서는 기본적으로 내가 지정한 Enum 사용
// gRPC 통신 시에만 mapper를 통해 변환해서 생성된 enum 사용

// App(문자열) -> gRPC(숫자) 변환

export const toGrpcRole: Record<AppRole, GrpcRole> = {
    [AppRole.USER]: GrpcRole.USER,
    [AppRole.ADMIN]: GrpcRole.ADMIN,
};


// gRPC(숫자) -> App(문자열) 변환 함수

export function toAppRole(grpcRole: GrpcRole): AppRole {
    switch (grpcRole) {
        case GrpcRole.ADMIN:
            return AppRole.ADMIN;
        case GrpcRole.USER:
            return AppRole.USER;
        case GrpcRole.UNSPECIFIED:
        case GrpcRole.UNRECOGNIZED:
        default:
            // 예외 상황
            return AppRole.USER; 
    }
}