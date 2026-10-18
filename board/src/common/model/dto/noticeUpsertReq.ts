import z from "zod";

import { BoardState } from "../enum/boardState";

// 공지 추가 / 수정

// Req.Body
export const UpsertNoticeReq = z.object({
  title: z.string().max(100),
  state: z.enum(BoardState).default(BoardState.PUBLIC),
  isPinned: z.boolean().default(false),
  content: z.string().max(10000),
});

// Req.Param : IdxParamReq

export type UpsertNoticeType = z.infer<typeof UpsertNoticeReq>;

// Res : NoticeDetailRes
