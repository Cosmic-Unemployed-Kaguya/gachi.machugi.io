import { Timestamp } from "typeorm";
import { BoardState } from "../enum/boardState";


export interface CommentListRes{
    idx: number,
    parent: number | null,
    userNickName: string,
    content : string,
    state : BoardState,
    updatedAt : Timestamp,

    level: number,
    children : CommentListRes[],
};
