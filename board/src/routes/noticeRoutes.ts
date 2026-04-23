import { Router } from 'express';
import { getUserAndRoleCheck, getUserInfo, getUserIdx } from './../middlewares/userCheck';
import { validate } from '../middlewares/validate';
import {addNotice, deleteNotice, getNoticeDetail, getNoticeList, updateNotice} from '../controller/noticeController'
import { PagingReq } from '../model/dto/paging';
import { UpsertNoticeReq } from '../model/dto/upsertNoticeReq';
import { BoardIdxParamReq, CommentIdxParamReq } from '../model/dto/IdxParamReq';
import { addComment, deleteComment, getComment, updateComment } from '../controller/commentController';
import { UserRole } from '../model/enum/userRole';
import { UpsertCommentReq } from '../model/dto/upsertCommentReq';
const route = Router();

/** 
 * 모든 공지 조회, 추가
 */
route
    .route('/')
    .get(validate({query : PagingReq}), getNoticeList)
    .post(validate({body: UpsertNoticeReq}), getUserAndRoleCheck([UserRole.ADMIN]), addNotice)

/**
 * 공지 상세 조회, 수정, 삭제 
 */
route
    .route('/:boardIdx')
    // 상세 조회의 경우 로그인 확인 x 
    .get(validate({params : BoardIdxParamReq}), getNoticeDetail)

    .put(validate({body: UpsertNoticeReq, params: BoardIdxParamReq}), 
        getUserAndRoleCheck([UserRole.ADMIN]), updateNotice)

    .delete(validate({params : BoardIdxParamReq}), getUserAndRoleCheck([UserRole.ADMIN]), deleteNotice)

/**
 *  댓글 조회, 추가
  */
route
    .route('/:boardIdx/comment')
    // 로그인 확인 x
    .get(validate({params : BoardIdxParamReq, query : PagingReq}),getComment)
    .post(validate({params : BoardIdxParamReq, body : UpsertCommentReq}), getUserInfo, addComment)

/** 
 * 댓글 수정, 삭제 
 */
route
    .route('/comment/:commentIdx')
    // 본인 확인은 db 조회가 필요하기에 서비스 내에서
    .put(validate({params : CommentIdxParamReq , body : UpsertCommentReq}), getUserInfo, updateComment )
    .delete(validate({params : CommentIdxParamReq }), getUserIdx , deleteComment )

export default route;