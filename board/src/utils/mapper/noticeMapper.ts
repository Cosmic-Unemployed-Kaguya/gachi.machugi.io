import { NoticeDetailRes } from "../../model/dto/noticeDetailDTO"
import { NoticeListRes } from "../../model/dto/noticeListDTO"
import { BoardCommentEntity } from "../../model/entity/boardComment"
import { BoardEntity } from "../../model/entity/boardEntity"

export const toNoticeDetail = (boardEntity : BoardEntity) : NoticeDetailRes=>{
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