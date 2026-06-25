
import { GrpcNoticeDetailResponse, GrpcNoticeListResponse, GrpcNoticePageResponse } from "../../generated/machugi/board/notice"
import { NoticeDetailRes } from "../../model/dto/noticeDetailDTO"
import { NoticeListRes } from "../../model/dto/noticeListDTO"
import { Page } from "../../model/dto/paging"
import { BoardCommentEntity } from "../../model/entity/boardComment"
import { BoardEntity } from "../../model/entity/boardEntity"
import { toAppState, toGrpcState } from "./boardStateMapper"

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

export const toGrpcNoticePage = (pagingData : Page<NoticeListRes>) : GrpcNoticePageResponse => {

    return {
        items : pagingData.items.map(
            (item) : GrpcNoticeListResponse =>({
                idx:item.idx,
                isPinned:item.isPinned,
                state: toGrpcState[item.state],
                title: item.title,
                updatedAt: item.updatedAt ? new Date(item.updatedAt as any) : undefined,
                viewCount: item.viewCount,

            })
        ),
        currentPage : pagingData.currentPage,
        hasNext : pagingData.hasNext,
        totalCount : pagingData.totalCount,
        totalPages : pagingData.totalPages,

    }
} 

export const toGrpcNoticeDetail = (noticeDetail : NoticeDetailRes) : GrpcNoticeDetailResponse =>{
    return {
        idx: noticeDetail.idx,
        content : noticeDetail.content,
        isPinned : noticeDetail.isPinned,
        state : toGrpcState[noticeDetail.state],
        title : noticeDetail.title,
        updatedAt :noticeDetail.updatedAt? noticeDetail.updatedAt : undefined,
        viewCount : noticeDetail.viewCount
    }
}