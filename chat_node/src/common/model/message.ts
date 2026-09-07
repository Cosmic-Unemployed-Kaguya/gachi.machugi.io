

// export interface MessageReq{
//     msg: string;
// }

import z from "zod";

export const MessageReq = z.object({
    // @TODO 최대 길이 설정
    msg : z.string().max(200),
});
export type MessageReq = z.infer<typeof MessageReq>;

export interface MessageRes{
    msg: string; 
    userNickname : string;
}