import { UserData } from "../../middlewares/appRequest";

/**
 * grpc 요청 처리 시 기존 req 데이터에 유저 정보를 포함시키기 위함
 */
export type AuthRequest<Treq extends object> = Treq & { userData?: UserData };
