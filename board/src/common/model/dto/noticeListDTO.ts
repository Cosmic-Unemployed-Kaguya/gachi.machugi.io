import { Timestamp } from "typeorm";

import { BoardEntity } from "../entity/boardEntity";
import { BoardState } from "../enum/boardState";

// 전체 공지 목록

// Req : PagingReq

// Res
export interface NoticeListRes {
  idx: number;
  title: string;
  state: BoardState;
  viewCount: number;
  isPinned: boolean;
  updatedAt: Date;
}
