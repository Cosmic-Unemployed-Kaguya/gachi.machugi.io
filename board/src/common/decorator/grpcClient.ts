import { Client, credentials } from "@grpc/grpc-js";
import Container, { Service } from "typedi";

import logger from "../utils/logger";

/**
 * GrpcCient 구현 클래스 데코레이터
 * - 해당 클래스를 컨테이너에 등록
 * - proto로 정의 된 client를 생성 및 컨테이너에 등록
 * @param address
 * @returns
 */
export function GrpcClient(
  clientClass: typeof Client,
  address: string,
): ClassDecorator {
  return function (constructor: Function) {
    try {
      const client = new clientClass(address, credentials.createInsecure());

      Reflect.defineMetadata("grpc-client-class", clientClass, constructor);

      Container.set(clientClass, client);

      Service()(constructor);
    } catch (err) {
      logger.error("gRPC Client 처리 중 에러 발생 : ", err);
      throw err;
    }
  };
}

/**
 * 해당 프로퍼티를 조회 할 때의 작동
 * - clientClass 타입 조회 및 컨테이너에서 객체 조회
 * - 프로퍼티에 정의 된 req를 통해 clientClass에 정의 된 함수 실행
 * @returns x
 */
export function GrpcClientProperty(): PropertyDecorator {
  return (target: any, propertyKey: string | symbol) => {
    const methodName = String(propertyKey);

    Object.defineProperty(target, propertyKey, {
      get() {
        const clientClass = Reflect.getMetadata(
          "grpc-client-class",
          target.constructor,
        );

        const targetClient = Container.get(clientClass) as any;

        return (request: any) => {
          return new Promise((resolve, reject) => {
            targetClient[methodName].bind(targetClient)(
              request,
              (err: any, response: any) => {
                if (err) {
                  reject(err);
                } else {
                  resolve(response);
                }
              },
            );
          });
        };
      },
      // 옵션 변경 : 기본 false 인데
      enumerable: true,
      configurable: true,
    });
  };
}
