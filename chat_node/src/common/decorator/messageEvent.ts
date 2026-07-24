import { EventMetadata } from '@common/model/event';
import 'reflect-metadata';

export function MessageEvent( event : string ) : MethodDecorator {
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

    }


}