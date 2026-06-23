import { Timestamp } from "typeorm";
import { BoardState } from "../enum/boardState";
import { BoardEntity } from "../entity/boardEntity";
import z from "zod";
import { BoardCommentEntity } from '../entity/boardComment';


// 공지 상세 조회


// Req.Param : IdxParamReq

// Res
export interface NoticeDetailRes{
    idx: number,
    title : string,
    state : BoardState,
    viewCount : number,
    isPinned: boolean,
    updatedAt : Date,
    content: string, 

}
