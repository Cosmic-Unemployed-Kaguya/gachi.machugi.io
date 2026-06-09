import { Timestamp } from "typeorm";
import { BoardState } from "../enum/boardState";
import { BoardEntity } from "../entity/boardEntity";

// 전체 공지 목록

// Req : PagingReq

// Res
export interface CommentListRes{
    idx: number,
    parent: number | null,
    // idx : nickName 으로 보낼지 , nickName으로 보낼지 ?? user Id를 사용할 지?????
    userNickName: string,
    content : string,
    state : BoardState,
    updatedAt : Date,
};
