import z from "zod"

export const GameStartReq = z.object({
    roomIdx :  z.coerce.number().int().positive()
})

export type GameStartReq = z.infer<typeof GameStartReq>