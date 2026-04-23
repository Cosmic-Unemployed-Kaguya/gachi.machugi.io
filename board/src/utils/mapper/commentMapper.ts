import { CommentListRes } from "../../model/dto/commentListRes";
import { CommentRes } from "../../model/dto/commentRes";
import { BoardCommentEntity } from "../../model/entity/boardComment";


export const toCommentListRes = (entity : BoardCommentEntity, userNicknameMap : Record<number, string>) : CommentListRes =>{
    return {
        idx: entity.idx,
        parent : entity.parent? entity.parent.idx : null,
        userNickName : userNicknameMap[entity.userIdx],
        content : entity.content,
        state : entity.state,
        updatedAt : entity.updatedAt,
    }
}

export const toCommentRes = (entity : BoardCommentEntity, userNickName : string  ) : CommentRes =>{
    return {
        idx: entity.idx,
        parent : entity.parent? entity.parent.idx : null,
        userNickName : userNickName,
        content : entity.content,
        state : entity.state,
        updatedAt : entity.updatedAt,
    }
}