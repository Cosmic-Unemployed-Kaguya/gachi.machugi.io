import { BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { WebSocket } from "ws";


export class Room{

    private sockets :  Set<CustomSocket> = new Set();
    // private kickedUsers : Set<number> = new Set();

    private currentAnswer : string[];

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
    
    public setAnswer(answer : string[]){
        this.currentAnswer = answer;
    }

    public checkAnswer(userAnswer : string) : boolean{

        // 정답 확인 로직!

        // 1. 유저 입력값을 normalize
        const normalUserAnswer = this.normalizeAnswer(userAnswer);

        // 1.1 만약 없으면 false
        if(!normalUserAnswer|| normalUserAnswer.length === 0) return false;


        // 2. 저장 되어있는 정답들을 normalize 하며 비교
        // .some > .map과 유사하게 배열 내 모든 값을 반복하지만, 하나라도 true가 나오면 true를 반환하며 순환 종료
        const correct = this.currentAnswer.some(answer => {
            const normalAnswer = this.normalizeAnswer(answer);
            return normalAnswer === normalUserAnswer;
        })
        
        return correct;
    }

    private normalizeAnswer(answer: string): string | null {
        if (!answer) {
            return null;
        }

        // 모든 공백 제거, 소문자로 변환
        return answer.replace(/\s+/g, "").toLowerCase();
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