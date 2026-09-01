import { BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { WebSocket } from "ws";


export class Room{

    private sockets :  Set<CustomSocket> = new Set();
    // private kickedUsers : Set<number> = new Set();

    private currentAnswer : string;

    constructor(){}

    public getUsers(){
        return this.sockets
    }

    public enterUser(socket : CustomSocket){
        // if(this.kickedUsers.has(socket.userIdx)) {
        //     throw WsError.fromType('USER_KICKED')
        // }
        this.sockets.add(socket);
    }

    public exitUser(socket : CustomSocket) {
        this.sockets.delete(socket);
    }

    public async sendMessage(data : BaseRes ){

        this.sockets.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(data))
            }      
        })
    }


    public destroy(){
        this.sockets.forEach( user => {
            user.roomIdx = undefined;
        } )
        this.sockets.clear();
    }
    
    public setAnswer(answer : string){
        this.currentAnswer = answer;
    }

    public checkAnswer(userAnswer : string) : boolean{
        // @TODO 정답 확인 로직~!~!~
        if(userAnswer == this.currentAnswer) return true
        
        return false
    }

    // public async kickUserByIdx(userIdx : number){
    //     this.sockets.forEach((socket) => {
    //         if(socket.userIdx == userIdx) {
    //             this.sockets.delete(socket);
    //             this.kickedUsers.add(socket.userIdx);
    //             socket.send(JSON.stringify(
    //                 {
    //                     event : 'kick'
    //                 } as BaseRes
    //             ))
    //         }
    //         return;
    //     })
    // }
    
    
}