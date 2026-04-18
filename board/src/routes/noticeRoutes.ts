import { Router } from 'express';
import { userCheck, userRoleCheck } from './../middlewares/userCheck';
import { validate } from '../middlewares/validate';
import {addNotice, deleteNotice, getNoticeDetail, getNoticeList, updateNotice} from '../controller/noticeController'
import { PagingReq } from '../model/dto/paging';
import { UpsertNoticeReq } from '../model/dto/upsertNoticeReq';
import { UserRole } from '../model/enum/userRole';
import { IdxParamReq } from '../model/dto/IdxParamReq';
import { getComment } from '../controller/commentController';
const route = Router();

/** 
 * 모든 공지 조회, 추가
 */
route
    .route('/')
    .get(validate({query : PagingReq}), getNoticeList)
    .post(validate({body: UpsertNoticeReq}), userRoleCheck([UserRole.ADMIN]), addNotice)

/**
 * 공지 상세 조회, 수정, 삭제 
 */
route
    .route('/:idx')
    // 상세 조회의 경우 로그인 확인 x 
    .get(validate({params : IdxParamReq}), getNoticeDetail)

    .put(validate({body: UpsertNoticeReq, params: IdxParamReq}), 
        userRoleCheck([UserRole.ADMIN]), updateNotice)

    .delete(validate({params : IdxParamReq}), userRoleCheck, deleteNotice)

/**
 *  댓글 조회, 추가
  */
// route
//     .route('/:noticeIdx/comment')
//     // 로그인 확인 x
//     .get(validate({params : IdxParamReq}), getComment)
//     .post()

// /** 
//  * 댓글 수정, 삭제 
//  */
// route
//     .route('/comment/:commentIdx')
//     .put(userCheck)
//     .delete(userCheck)

export default route;