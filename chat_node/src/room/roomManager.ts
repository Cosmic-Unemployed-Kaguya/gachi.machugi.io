import { BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { Service } from "typedi";
import { Room } from "./room";

@Service()
export class RoomManager{

    // Map < roomIdx , Room >
    private rooms :Map<number, Room> = new Map;


    public async sendMessage(roomIdx: number, data: BaseRes){
        
        const room = this.rooms.get(roomIdx)

        if(!room){
            // @TODO 에러처리 
            // 그런 room은 세상에 존재하지않아요!!!!!!!!!!!!!
            return;
        }
        await room.sendMessage(data);
    }


    public createAndJoinRoom(roomIdx : number, socket : CustomSocket) {


        // 이미 해당 방에 들어가 있는 경우
        if(socket.roomIdx == roomIdx) {
            // TODO 님 이미 들어가있음 처리
            
        }
        // 다른 방에 들어가 있는 경우
        if(socket.roomIdx){
            // TODO 퇴장 먼저 하셈 처리
            // 여기서 바로 옮겨줄지? 사용자가 퇴장 후 재입장 하도록 할지?
            // 일단 바로 옮겨줍시다

            const prevRoom = this.rooms.get(socket.roomIdx);

            if (!prevRoom) throw new Error('존재 하지 않는 방. 뭔가뭔가 오류가 생김!@!@!');

            socket.roomIdx = undefined;
            prevRoom.exitUser(socket);
        }
        

        const room = this.rooms.get(roomIdx)

        // 없으면 방 새로 만들고 입장
        if(!room){
            const newRoom = new Room();         

            newRoom.enterUser(socket);
            socket.roomIdx = roomIdx;

            this.rooms.set(roomIdx, newRoom);

        }else{
            room.enterUser(socket);
            socket.roomIdx = roomIdx;
        }



    }

    public exitRoom( socket : CustomSocket){

        if(!socket.roomIdx){
            // TODO 임시
            throw new Error('방에 들어가있지않습니다')
        }

        const room = this.rooms.get(socket.roomIdx);
        if(!room){
            // TODO 임시
            throw new Error('존재 하지 않는 방. 뭔가뭔가 오류가 생김!@!@!')
        }

        socket.roomIdx = undefined;
        room.exitUser(socket);

    }

    
    public async kickUser( roomIdx: number , userIdx : number ){

        const room = this.rooms.get(roomIdx);

        if(!room){
            return;
        }
        
        await room.kickUserByIdx(userIdx);

    }
    
}