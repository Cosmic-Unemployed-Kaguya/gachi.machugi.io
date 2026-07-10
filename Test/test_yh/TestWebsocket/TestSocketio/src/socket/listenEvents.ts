import { JoinRoomReq } from "../dto/joinRoom";
import { MessageReq } from "../dto/message";

export interface ClientToServerEvents {
    // notice: (msg: string) => void;
    join_room: (data : JoinRoomReq) => void;
    message: (data: MessageReq) => void;
    exit_room : () => void;
}