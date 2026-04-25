import { Router } from 'express';
import { validate } from '../middlewares/validate';
import { addQuizComment, deleteQuizComment, getQuizComment, getQuizCommentReplies, updateQuizComment } from '../controller/quizCommentController';
import { CommentIdxParamReq, QuizIdxParamReq } from '../model/dto/idxParamReq';
import { PagingReq } from '../model/dto/paging';
import { UpsertCommentReq } from '../model/dto/upsertCommentReq';
import { getUserIdx, getUserInfo } from '../middlewares/userCheck';

const route = Router();

/**
 *  댓글 조회, 추가
  */
route
    .route('/:quizIdx/comment')
    // 로그인 확인 x
    .get(validate({params : QuizIdxParamReq, query : PagingReq}),getQuizComment)
    .post(validate({params : QuizIdxParamReq, body : UpsertCommentReq}), getUserInfo, addQuizComment)

/** 
 * 댓글 수정, 삭제 
 */
route
    .route('/comment/:commentIdx')
    // 대댓글~ 조회
    .get(validate({params : CommentIdxParamReq, query : PagingReq}), getQuizCommentReplies)
    // 본인 확인은 db 조회가 필요하기에 서비스 내에서
    .put(validate({params : CommentIdxParamReq , body : UpsertCommentReq}), getUserInfo, updateQuizComment )
    .delete(validate({params : CommentIdxParamReq }), getUserIdx , deleteQuizComment )

export default route;