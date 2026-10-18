import z from "zod";


// export interface JoinRoomReq{
//     roomIdx : number;
// }
const uuidV4Regex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/;

export const JoinRoomReq = z.object({
    roomIdx :  z.coerce.number().int().positive(),
    ticketUuid: z.string().regex(uuidV4Regex, "유효하지 않은 티켓 형식입니다."),
})

export type JoinRoomReq = z.infer<typeof JoinRoomReq>

export interface JoinSuccessRes{
    userNickname :  string;
}