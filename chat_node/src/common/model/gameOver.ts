import z from "zod"

export const GameOverReq = z.object({
    // 아직 미정
})

export type GameOverReq = z.infer<typeof GameOverReq>