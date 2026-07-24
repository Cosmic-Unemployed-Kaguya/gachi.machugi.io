import { BaseReq } from '@common/model/base';
import { CustomSocket } from '@common/model/customSocket';
import { EventMap, EventMetadata } from '@common/model/event';
import logger from '@common/util/logger';
import 'reflect-metadata';
import { Inject, Service } from "typedi";
import { RawData, Server } from "ws";
import { MessageEventHandler } from '../../websocket/messageEventHandler';
import { ServerEventHandler } from '../../websocket/serverEventHandler';
import { SocketEventHandler } from '../../websocket/socketEventHandler';

@Service()
export class WsLoader {

    private serverEvList : EventMetadata[];
    private socketEvList : EventMetadata[];
    private messageEvList: EventMap = new Map(); 

    constructor(
        @Inject() private ws : Server ,
        @Inject() private serverHandler : ServerEventHandler,
        @Inject() private socketHandler : SocketEventHandler, 
        @Inject() private messageHandler: MessageEventHandler
    ){
        this.bindServerEv()
    }

    private bindServerEv(){

        this.serverEvList = Reflect.getMetadata('ws:server', this.serverHandler.constructor)
        this.socketEvList = Reflect.getMetadata('ws:socket', this.socketHandler.constructor)

        const rawMessageMeta: EventMetadata[] = Reflect.getMetadata('ws:message', this.messageHandler.constructor) || [];
            rawMessageMeta.forEach(ev => {
                this.messageEvList.set(ev.event, ev.methodName);
            });

            
        this.serverEvList.forEach(serverEv => {

            
            if (serverEv.event === 'connection'){
                this.ws.on('connection' ,  (socket : CustomSocket, request) => {
                    (this.serverHandler as any)[serverEv.methodName].bind(this.serverHandler)(socket, request)
                    
                    this.bindSocketEv(socket);
                    
                })
            }else{
                this.ws.on(serverEv.event , (this.serverHandler as any)[serverEv.methodName].bind(this.serverHandler))
            }

        });


        if (!this.serverEvList.some(ev => ev.event === 'connection')) {
            this.ws.on('connection', (socket: CustomSocket) => {
                this.bindSocketEv(socket);
            });

        }

    }

    private bindSocketEv(socket : CustomSocket){

        socket.on('message' , (data : RawData) => {
            try{
                const strData = data.toString('utf-8');
                const baseReq: BaseReq = JSON.parse(strData);

                this.bindMessageEv(socket ,baseReq)
            }catch(error){
                // 임시
                logger.error(error)
                throw new Error('비!!!!!상!!!!!!!');
                
                
            }

        })

        this.socketEvList.forEach( socketEv => {
            socket.on(socketEv.event , (this.socketHandler as any)[socketEv.methodName].bind(this.socketHandler, socket))

        } )

    }

    private bindMessageEv( socket : CustomSocket ,baseReq : BaseReq){
        
        const methodName = this.messageEvList.get(baseReq.event);

        if(!methodName){
            // 임시
            throw new Error('진짜 큰일남!!!');
        }

        (this.messageHandler as any)[methodName].bind(this.messageHandler)(socket, baseReq)
        

    }



} 
