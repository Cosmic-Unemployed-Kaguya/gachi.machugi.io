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

    public async sendMessage(data : any ){

        this.sockets.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(data))
            }      
        })
    }
    
    
}