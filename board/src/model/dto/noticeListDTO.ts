import { Timestamp } from "typeorm";
import { BoardState } from "../enum/boardState";
import { BoardEntity } from "../entity/boardEntity";

// 전체 공지 목록

// Req : PagingReq

// Res
export interface NoticeListRes{
    idx: number,
    title : string,
    state : BoardState,
    viewCount : number,
    isPinned: boolean,
    updatedAt : Timestamp,
};

export const toNoticeListRes = (entity : BoardEntity) : NoticeListRes => {

    return {
        idx : entity.idx,
        isPinned : entity.isPinned,
        state : entity.state,
        title : entity.title,
        updatedAt : entity.updatedAt,
        viewCount :entity.viewCount,
    }
}