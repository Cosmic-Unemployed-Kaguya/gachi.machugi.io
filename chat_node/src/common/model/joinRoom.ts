import z from "zod";


// export interface JoinRoomReq{
//     roomIdx : number;
// }

export const JoinRoomReq = z.object({
    roomIdx :  z.coerce.number().int().positive(),
})
export type JoinRoomReq = z.infer<typeof JoinRoomReq>

export interface JoinSuccessRes{
    userNickname :  string;
}