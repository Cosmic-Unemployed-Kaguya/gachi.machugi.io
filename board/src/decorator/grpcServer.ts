import { Service, ServiceOptions } from "typedi";
import { grpcErrorHandler } from "../utils/grpcErrorHandler";

/**
 * gRPC 서버 클래스를 정의하는 데코레이터
 * 
 * 1. metadate에 protoService 저장
 *  - 이후 타 모듈에서 조회 후 서버로 등록하기 위함
 * 2. DI컨테이너에 등록
 * 
 * 3. 아래 메서드들에 GrpcServerMethod 데코레이터 주입
 * 
 * @param serviceDefinition : proto Service 객체
 * @param options? : DI컨테이너 설정
 * @returns x
 */
export function GrpcServer(serviceDefinition : any , options?: ServiceOptions) :ClassDecorator{
    return function (constructor: Function) {
        
        // # 해당 클래스의 grpc-server 메타데이터에 serviceDefinition 등록
        Reflect.defineMetadata("grpc-server", serviceDefinition ,constructor);
        
        // # 아래로는 클래스 내 메서드들에 처리할 내용
        // 0. 감쌀 메서드 데코레이터를 미리 가져옴
        const methodDecorator = GrpcServerMethod()
        // 1. 해당 클래스 아래 메서드 이름을 가져옴 
        const fnNames: string[] =  Object.getOwnPropertyNames(constructor.prototype);

        // 2. 모든 메서드 순회
        for (const fnName of fnNames){
            // 2.1 생성자는 패스
            if (fnName === 'constructor') continue;

            // 3. 클래스 내 해당 이름의(fnName) 메서드의 설명서를 반환 (메서드 데코레이터의 3번째 파라미터와 동일)
            const fnDescriptor = Object.getOwnPropertyDescriptor(constructor.prototype ,fnName ) 

            if (fnDescriptor){
                // 4. 위에서 가져왔던 메서드 데코레이터에 값을 채워줌, 데코레이터를 씌운 새로운 설명서 가져옴
                const newDescriptor = methodDecorator(constructor.prototype, fnName, fnDescriptor);

                // 5. 해당 메서드를 새로운 설명서로 정의
                Object.defineProperty(constructor.prototype, fnName, newDescriptor || fnDescriptor);
            }

        }

        // # 해당 클래스를 DI컨테이너에 등록 
        Service(options)(constructor); 
    };
}


/**
 * ### gRPC 메서드 코드의 단축을 위한 데코레이터 
 * 기존에는 call, callback을 파라미터로 받아서 callback에 data를 담아서 실행하는 지루하고 현학적인 구현을 해야함.
 * 이를 반복하지 않기 위한 데코레이터
 * 
 * 1. call, callback을 파라미터로 받아 req에 call.request를 넣어주는 함수로 원본 함수를 감쌈
 * 
 * 2. 구현해야하는 함수는 req를 받아 res를 반환하는 단순한 함수 (== controller)
 * 
 * 3. 반환 된 res를 받아 callback에 담아서 실행
 * 
 * @returns x 
 */
export function GrpcServerMethod(): MethodDecorator {
    return function (target: any, property: string | symbol, descriptor: PropertyDescriptor) {
        let originalMethod = descriptor.value;
        
        descriptor.value = async function (call : any , callback : any){
            try{
                // 본래 함수 실행 전, call에서 request와 metadata를 꺼내 넣어줌
                const req = call.request;
                const metadata = call.metadata;
                const res  = await originalMethod.call(this, req, metadata);
                // 이후 받아온 res로 callback 실행
                callback(null, res);

            }catch(err){
                const errPayload = grpcErrorHandler(err);
                callback(errPayload, null)
            }
        }
        return descriptor;
    }

}