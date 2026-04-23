import z from "zod";

// Req.Param
export const BoardIdxParamReq = z.object({
    boardIdx: z.coerce.number().int().positive(), 
})

export type BoardIdxParamReqType = z.infer<typeof BoardIdxParamReq>;

export const CommentIdxParamReq = z.object({
    commentIdx: z.coerce.number().int().positive(), 
})

export type CommentIdxParamReqType = z.infer<typeof CommentIdxParamReq>;