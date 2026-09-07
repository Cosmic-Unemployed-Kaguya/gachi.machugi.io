import { BaseReq } from '@common/model/base';
import { CustomSocket } from '@common/model/customSocket';
import { EventMetadata, MessageEventType } from '@common/model/event';
import 'reflect-metadata';
import z from 'zod';

// @TODO schema를 받을 수 밖에 없나??????

export function MessageEvent( event : MessageEventType ,schema?: z.ZodTypeAny) : MethodDecorator {
    return function (
        target: any,
        property: string | symbol,
        descriptor: PropertyDescriptor
    ){
        // 1. 먼저 등록 된 이벤트 메서드 있는지?
        const messageEv : EventMetadata[] = Reflect.getMetadata('ws:message', target.constructor) || [];
        
        // 2. 메타데이터에 이벤트명, 함수 이름 등록
        messageEv.push({ event : event, methodName : property })
        Reflect.defineMetadata('ws:message', messageEv, target.constructor )


        // 3. 기존 socket, baseReq를 파라미터로 받는 함수를 적어도 코드에서는 더 명확한 DTO 구조를 보이게
        let originalMethod = descriptor.value;

        descriptor.value = async function (socket : CustomSocket, baseReq : BaseReq) {
            
            let data = baseReq.data
            
            // 유효성 검사
            if(schema){
                data = schema.parseAsync(baseReq.data);
            }

            await originalMethod.call(this, socket, data);
            
        }
    }


}