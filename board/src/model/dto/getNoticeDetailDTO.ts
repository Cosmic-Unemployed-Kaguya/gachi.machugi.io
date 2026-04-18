import { Timestamp } from "typeorm";
import { BoardState } from "../enum/boardState";
import { BoardEntity } from "../entity/boardEntity";
import z from "zod";
import { BoardCommentEntity } from './../entity/boardComment';


// 공지 상세 조회


// Req.Param : IdxParamReq

// Res
export interface NoticeDetailRes{
    idx: number,
    title : string,
    state : BoardState,
    viewCount : number,
    isPinned: boolean,
    updatedAt : Timestamp,
    content: string, 

    // --------------
    /** @TODO 이후 댓글 추가 */ 
}

export const toNoticeDetail = (boardEntity : BoardEntity , boardCommentEntityList? : BoardCommentEntity[] ) : NoticeDetailRes=>{
    return {
        idx: boardEntity.idx,
        title: boardEntity.title,
        content :boardEntity.content,
        isPinned : boardEntity.isPinned,
        state : boardEntity.state,
        updatedAt : boardEntity.updatedAt,
        viewCount : boardEntity.viewCount,
    }
}