import { BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import logger from "@common/util/logger";
import { Service } from "typedi";
import { WsError } from './../common/error/wsError';
import { Room } from "./room";

@Service()
export class RoomManager{

    // Map < roomIdx , Room >
    private rooms :Map<number, Room> = new Map;


    public async sendMessage(roomIdx: number, data: BaseRes){
        
        const room = this.rooms.get(roomIdx)

        // 해당 서버에 존재하지 않는 room. 
        if(!room){
            logger.debug('존재하지 않는 room 입니다')
            return;
            // throw WsError.fromType('ROOM_NOT_FOUND');
        }

        await room.sendMessage(data);
    }

    public createRoom(roomIdx : number){
        const room = this.rooms.get(roomIdx)
        if(room){
            throw WsError.fromType('ROOM_ALREADY_EXISTS');
        }
        
        const newRoom = new Room();
        this.rooms.set(roomIdx, newRoom);
    }

    public joinRoom(roomIdx : number, socket : CustomSocket) {


        // 이미 해당 방에 들어가 있는 경우
        if(socket.roomIdx == roomIdx) {
            throw WsError.fromType('ALREADY_IN_ROOM');
            
        }
        // 다른 방에 들어가 있는 경우
        if(socket.roomIdx){
            // 퇴장 먼저 하셈 처리
            // 여기서 바로 옮겨줄지? 사용자가 퇴장 후 재입장 하도록 할지?
            // 일단 바로 옮겨줍시다

            const prevRoom = this.rooms.get(socket.roomIdx);

            if (!prevRoom) throw WsError.fromType('ROOM_NOT_FOUND');

            socket.roomIdx = undefined;
            prevRoom.exitUser(socket);
        }
        

        const room = this.rooms.get(roomIdx)

        // 없으면 방 새로 만들고 입장
        // x 없으면 에러. 방 생성/삭제 로직의 트리거는 전적으로 Room서비스에 맡김. x
        if(!room){

            const newRoom = new Room();         

            newRoom.enterUser(socket);
            socket.roomIdx = roomIdx;

            this.rooms.set(roomIdx, newRoom);

            
            // throw WsError.fromType('ROOM_NOT_FOUND')

        }else{
            room.enterUser(socket);
            socket.roomIdx = roomIdx;
        }



    }

    public exitRoom( socket : CustomSocket){

        if(!socket.roomIdx){

            throw WsError.fromType('NOT_IN_ROOM')
        }

        const room = this.rooms.get(socket.roomIdx);
        if(!room){
            throw WsError.fromType('ROOM_NOT_FOUND')
        }

        socket.roomIdx = undefined;
        room.exitUser(socket);

    }
    
    public deleteRoom( roomIdx : number){
        const room = this.rooms.get(roomIdx);
        
        // 해당 서버에 해당 방이 없을 수 도 있음
        if (!room) return;

        // room 삭제 (일단 이정도만 하면 메모리에서 지워지는듯?)
        room.destroy();
        this.rooms.delete(roomIdx);
        
    }

    
    public async kickUser( roomIdx: number , userIdx : number ){

        const room = this.rooms.get(roomIdx);

        if(!room){
            throw WsError.fromType('ROOM_NOT_FOUND')
        }
        
        await room.kickUserByIdx(userIdx);

    }
    
}