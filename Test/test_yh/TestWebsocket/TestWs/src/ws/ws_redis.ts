
import os from 'os';
import { Server, WebSocket } from "ws";
import { BaseReq, BaseRes } from "../dto/base";
import { ExitRoomRes } from "../dto/exitRoom";
import { JoinRoomReq, JoinSuccessRes } from "../dto/joinRoom";
import { MessageReq, MessageRes } from "../dto/message";
import { pubClient, subClient } from "./redis";
import { CustomSocket } from "./ws";

const rooms = new Map<string,Set<CustomSocket>>();


subClient.subscribe('global_chat_channel'); 
subClient.on('message', (channel, message) => {
    

    const req = JSON.parse(message);
    const currentRoom = rooms.get(req.roomId);
    if(!currentRoom) return;
    currentRoom.forEach(client => {
        
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(req.data))
        }
    });
});

export default async ({ws} : {ws: Server}) =>{
    // room =  Map < roomId , socket > 

    // 연결
    ws.on('connection', (socket : CustomSocket, request) => {

        const headers = request.headers;

        const userIdxStr = headers['x-user-idx']; 
        const userRoleStr = headers['x-user-role'];
        
        socket.userIdx = Number(userIdxStr);
        
        // !!!! 테스트 기록 용 !!!!
        const podName = os.hostname(); 
        
        sendRedis(String(userIdxStr), {
            event: "on_connect",
            data: podName
        }as BaseRes)


        /**  @TODO 유저 닉네임 가져오기 (gRPC 추가해야함;;;;) */
        socket.userNickname = "temp"+ userIdxStr

        // 요청 수신 시 
        socket.on('message', data => {

            /** 필요한 수순
             * 1. 순수 문자열을 파싱해야함 > JSON
             * 2. 이벤트 구분 필요 (방 입장, 채팅, 방 퇴장)
             * 3. @TODO 유효성 검사!!! < 일단 스킵
             * 4. 
             * 
             */
            const strData = data.toString('utf-8');

            const baseReq: BaseReq = JSON.parse(String(strData));
            
            switch(baseReq.event){
                case "join_room": {

                    // @TODO 원래 zod를 쓰던 뭘 쓰던 유효성 검사 필요함
                    const req : JoinRoomReq =  baseReq.data
                    const roomId : string = req.roomId;

                    // 방이 원래 존재했음? 없으면 생성
                    if (!rooms.has(roomId)) {
                        rooms.set(roomId, new Set<CustomSocket>());
                    }

                    // 방 조회
                    const currentRoom =  rooms.get(roomId)
                    console.log(`test3`)
                    // 방에 추가
                    if (currentRoom) {
                        currentRoom.add(socket);
                    }
                    socket.roomId = roomId;

                    // 응답 데이터
                    const res : BaseRes = {
                        event: "join_room",
                        data : {userNickname : socket.userNickname} as JoinSuccessRes
                    }
                    
                    // redis로 전파
                    sendRedis(roomId ,res);

                    console.log(`방 참여 ${req.roomId}`)
                    break;
                }
                
                case "chat":{

                    // 보낸 이 방id
                    const roomId : string = socket.roomId;


                    // 요청 데이터
                    const msgReq : MessageReq = baseReq.data  

                    // 응답 데이터
                    const msgRes : MessageRes = {
                        msg : msgReq.msg,
                        userNickname : socket.userNickname
                    }

                    const res : BaseRes = {
                        event : "chat",
                        data : msgRes
                    }

                    // redis로 전파
                    sendRedis(roomId, res);

                    console.log(JSON.stringify(msgRes))
                    break;
                }

                case "exit_room":{

                    if(!socket.roomId) break;

                    // 보낸 이 방id
                    const roomId : string = socket.roomId;

                    const res = {
                        event: "exit_room",
                        data : {userNickname: socket.userNickname} as ExitRoomRes
                    }

                    sendRedis(roomId, res )


                    /** @TODO 임시 */
                    socket.roomId = "null"

                    
                    console.log('방 퇴장')
                    break;
                }
            }

        });


        // 연결 종료 시 
        socket.on('close', () => {
            console.log('Client disconnected');
        });
    });
    
}


export function sendRedis(roomId: string ,data:any, channel :string = 'global_chat_channel'){
    
    const payload ={
        roomId: roomId,
        data: data
    }
    pubClient.publish(channel, JSON.stringify(payload));
}