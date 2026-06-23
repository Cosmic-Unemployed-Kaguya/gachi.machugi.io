import { CommentPageRes as GrpcCommentPageRes, CommentRes as GrpcCommentRes} from "../../generated/notice";
import { CommentListRes as AppCommentListRes} from "../../model/dto/commentListRes";
import { CommentRes as AppCommentRes } from "../../model/dto/commentRes";
import { BoardCommentEntity } from "../../model/entity/boardComment";
import { QuizCommentEntity } from "../../model/entity/quizComment";
import { Page } from './../../model/dto/paging';


export const toCommentListRes = (entity : BoardCommentEntity | QuizCommentEntity, userNicknameMap : Record<number, string>) : AppCommentListRes =>{
    return {
        idx: entity.idx,
        parent : entity.parent? entity.parent.idx : null,
        userNickName : userNicknameMap[entity.userIdx],
        content : entity.content,
        state : entity.state,
        updatedAt : entity.updatedAt,
    }
}

export const toCommentRes = (entity : BoardCommentEntity | QuizCommentEntity , userNickName : string  ) : AppCommentRes =>{
    return {
        idx: entity.idx,
        parent : entity.parent? entity.parent.idx : null,
        userNickName : userNickName,
        content : entity.content,
        state : entity.state,
        updatedAt : entity.updatedAt,
    }
}

export const toGrpcCommentRes = (data : AppCommentRes ) : GrpcCommentRes =>{
    return {
        content:data.content,
        idx:data.idx,
        state:data.state,
        updatedAt:data.updatedAt,
        userNickName:data.userNickName,
        parent:data.parent? data.parent :undefined,
    }
}

export const toGrpcCommentPageRes = (pagingData : Page<AppCommentListRes>) : GrpcCommentPageRes=> {

    
        return {
            items : pagingData.items.map(
                (item) : GrpcCommentRes =>({
                    content:item.content,
                    idx:item.idx,
                    state:item.state,
                    updatedAt:item.updatedAt,
                    userNickName:item.userNickName,
                    parent:item.parent? item.parent :undefined,
                    
                })
            ),
            currentPage : pagingData.currentPage,
            hasNext : pagingData.hasNext,
            totalCount : pagingData.totalCount,
            totalPages : pagingData.totalPages,
    
        }

}