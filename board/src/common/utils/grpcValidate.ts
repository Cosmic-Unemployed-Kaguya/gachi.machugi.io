import { GrpcError } from "@cosmic-unemployed-kaguya/grpc-express";
import z from "zod";

/**
 *  요청값에 대해 내가 정의해둔 dto로 검증하는 함수
 *
 * @param dto  : 이 dto구조로 검증/변환을 하겠다
 * @param data : 검증 할 데이터 입력
 * @returns dto
 */
export async function grpcValidate<T>(dto: z.ZodSchema<T>, data: any) {
  try {
    return await dto.parseAsync(data);
  } catch {
    throw new GrpcError(3, "잘못된 요청 데이터 형식입니다.");
  }
}
