import { BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";


export class Room{

    private sockets :  Set<CustomSocket> = new Set();

    constructor(){}

    public enterUser(socket : CustomSocket){
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

    public async kickUserByIdx(userIdx : number){
        this.sockets.forEach((socket) => {
            if(socket.userIdx == userIdx) this.sockets.delete(socket);
            socket.send(JSON.stringify(
                {
                    event : 'kick'
                } as BaseRes
            ))
            return;
        })
    }
    
    
}