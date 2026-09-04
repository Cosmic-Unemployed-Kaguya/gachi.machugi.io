import { GrpcError } from "@cosmic-unemployed-kaguya/grpc-express";
import { Metadata } from "@grpc/grpc-js";

import { toAppRole } from "../mapper/userMapper";
import { UserData } from "../middlewares/appRequest";

import { AuthRequest } from "@dto/grpcBaseReq";

import { UserRole } from "@generated/machugi/board/user";

/**
 * 메타데이터를 까서 useridx와 role 이 있는지 확인,
 * 경우에 따라 권한 검사까지 실행
 * @param allowedRoles
 * @returns
 */
export function GrpcAuth(allowedRoles?: UserRole[]): MethodDecorator {
  return function (target: any, property: string | symbol, descriptor: PropertyDescriptor) {
    let originalMethod = descriptor.value;

    descriptor.value = async function (req: AuthRequest<object>, metadata: Metadata) {
      try {
        // 1. 메타데이터에서 userIdx 및 userRole 꺼내기
        const userIdxStr = getStrFromMeta(metadata, "x-user-idx");
        const userRoleStr = getStrFromMeta(metadata, "x-user-role");

        // 2. 데이터 존재 여부 확인
        if (!userIdxStr) {
          throw new GrpcError(16, "로그인이 필요합니다");
        }
        if (!userRoleStr) {
          throw new GrpcError(16, "잘못된 접근입니다");
        }

        // 3. 각 타입에 맞게 변환
        const userIdx = Number(userIdxStr);
        if (Number.isNaN(userIdx)) {
          throw new GrpcError(3, "잘못 된 id입니다");
        }

        const userRole: UserRole = roleMap[userRoleStr];
        if (!userRole) {
          throw new GrpcError(3, "잘못된 역할입니다");
        }

        // 3.5 역할 권환 확인
        if (allowedRoles && !allowedRoles.includes(userRole)) {
          throw new GrpcError(7, "권한이 없습니다");
        }

        const appRole = toAppRole(userRole);
        // 4. UserData 생성
        const userData: UserData = {
          userIdx: userIdx,
          userRole: appRole,
        };
        // 5. 요청데이터에 유저데이터 꽂아주기
        req.userData = userData;
      } catch (err) {
        if (err instanceof GrpcError) {
          throw err;
        }
        throw new GrpcError(13, "서버 인증 처리 중 오류가 발생했습니다.");
      }

      return await originalMethod.call(this, req);
    };
    return descriptor;
  };
}

const getStrFromMeta = (metaData: Metadata, key: string): string | undefined => {
  const rawValue = metaData.get(key)[0];

  if (!rawValue) {
    return undefined;
  }
  return Buffer.isBuffer(rawValue) ? rawValue.toString("utf-8") : rawValue;
};

const roleMap: Record<string, UserRole> = {
  user: UserRole.USER,
  USER: UserRole.USER,
  "1": UserRole.USER,

  admin: UserRole.ADMIN,
  ADMIN: UserRole.ADMIN,
  "2": UserRole.ADMIN,
};
