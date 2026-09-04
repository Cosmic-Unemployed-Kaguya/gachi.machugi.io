import { Router } from "express";

import { addQuizComment, deleteQuizComment, getQuizComment, getQuizCommentReplies, updateQuizComment } from "./quizCommentController";

import { getUserIdx, getUserInfo } from "@common/middlewares/userCheck";
import { validate } from "@common/middlewares/validate";

import { UpsertCommentReq } from "@dto/commentUpsertReq";
import { CommentIdxParamReq, QuizIdxParamReq } from "@dto/idxParamReq";
import { PagingReq } from "@dto/paging";

const route = Router();

/**
 *  댓글 조회, 추가
 */
route
  .route("/:quizIdx/comment")
  // 로그인 확인 x
  .get(validate({ params: QuizIdxParamReq, query: PagingReq }), getQuizComment)
  .post(validate({ params: QuizIdxParamReq, body: UpsertCommentReq }), getUserInfo, addQuizComment);

/**
 * 댓글 수정, 삭제
 */
route
  .route("/comment/:commentIdx")
  // 대댓글~ 조회
  .get(validate({ params: CommentIdxParamReq, query: PagingReq }), getQuizCommentReplies)
  // 본인 확인은 db 조회가 필요하기에 서비스 내에서
  .put(validate({ params: CommentIdxParamReq, body: UpsertCommentReq }), getUserInfo, updateQuizComment)
  .delete(validate({ params: CommentIdxParamReq }), getUserIdx, deleteQuizComment);

export default route;
