import { ExitRoomRes } from "../dto/exitRoom";
import { JoinSuccessRes } from "../dto/joinRoom";
import { MessageRes } from "../dto/message";
import { ServerInfo } from "../dto/test";

export interface ServerToClientEvents {
    // notice: (msg: string) => void;
    join_success: (data: JoinSuccessRes) => void;
    message: (data:  MessageRes ) => void;
    exit_room : (data: ExitRoomRes) => void;
    server_info :(data: ServerInfo) => void;
}