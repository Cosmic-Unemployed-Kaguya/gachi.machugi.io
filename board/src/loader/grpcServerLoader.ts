import { Server, ServerCredentials, status } from "@grpc/grpc-js";
import logger from "../utils/logger";
import setting from "../config"
import grpcServers from "../grpc_server";
import Container from "typedi";

// 서버 객체 생성  
export const server = new Server();

export const loadService = async() =>{
    // 0. gRPC server로 등록 된 클래스 순회
    for(const serverClass of  grpcServers){

        // 1. 클래스 메타데이터 내에  'grpc-server' 있음??
        const service = Reflect.getMetadata('grpc-server' ,serverClass );

        if (service){
            // 2. 있으면 컨테이너에서 원본 클래스 꺼내옴
            const serviceInstance = Container.get(serverClass) as any;
            
            // 3. 빈 record 객체 생성, 여기에 각 메서드들을 담을거임
            const implementation : Record<string, any> = {};
            
            // 4. 메서드 순회
            for(const methodName in service){
                // serviceInstance[methodName] == serviceInstance.{methodName} 즉 클래스 내 메서드에 접근한것과 마찬가지
                if(typeof serviceInstance[methodName] === 'function'){
                    // !!! 중요 !!!
                    // 빈 implementation 에 {메서드이름: 메서드} 로 데이터를 넣음
                    // .bind(~~) : 해당 메서드를 ~~에 완전히 묶어?버리는것
                    // > 그냥 메서드만 넘기면 나중에 꺼내쓸때 메서드 내 this의 위치가 바뀌거나 없어짐
                    // > 이를 방지하고자 기존 주인(클래스)를 명확하게 새기는 작업이 필요하다고 함
                    implementation[methodName] = serviceInstance[methodName].bind(serviceInstance);
                }else{
                    
                    /** @TODO  이 아래. 임시코드*/
                    logger.warn(` ${methodName} 메서드가 미구현 상태입니다.`);
            
                    // 미구현 함수에 대해 임시로 에러를 뱉는 구현체 만들어 두기
                    implementation[methodName] = async function (call: any, callback: any) {
                        // 요청 오면 에러를 뱉음
                        callback({
                            code: status.UNIMPLEMENTED,
                            details: "개발 주우우우웅."
                        }, null);
                    };
                                
                }
            }
            // 5. 서비스로 등록
            server.addService(service, implementation)

        }
    }
}

export const startServer = async () => {
    server.bindAsync(setting.grpcServerAddress, ServerCredentials.createInsecure(),
    (err, port) =>{
        if(err){
            logger.error("grpc 서버 실행 중 에러 ", err);
            return;
        }
        logger.info("%d 번 포트로 grpc 서버 실행 중", port);
    });
} 
