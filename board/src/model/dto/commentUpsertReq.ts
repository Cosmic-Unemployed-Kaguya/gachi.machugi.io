import z from "zod";
import { BoardState } from "../enum/boardState";

// 댓글 추가 / 수정



// Req.Body
export const UpsertCommentReq = z.object({
    content : z.string().max(2000),
    state : z.enum(BoardState).default(BoardState.PUBLIC),
    parent : z.coerce.number().int().positive().nullish(),
})

// Req.Param : IdxParamReq

export type UpsertCommentReqType = z.infer<typeof UpsertCommentReq>;