
import { BoardState } from "../enum/boardState";

// 공지 상세 조회

// Req.Param : IdxParamReq

// Res
export interface NoticeDetailRes {
  idx: number;
  title: string;
  state: BoardState;
  viewCount: number;
  isPinned: boolean;
  updatedAt: Date;
  content: string;
}
