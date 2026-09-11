import { Router } from "express";

import { addComment, deleteComment, getComment, getCommentReplies, updateComment } from "../board-comment/boardCommentController";

import { getUserIdx, getUserInfo } from "@common/middlewares/userCheck";
import { validate } from "@common/middlewares/validate";

import { UpsertCommentReq } from "@dto/commentUpsertReq";
import { BoardIdxParamReq, CommentIdxParamReq } from "@dto/idxParamReq";
import { PagingReq } from "@dto/paging";

const route = Router();

/**
 *  댓글 조회, 추가
 */
route
  .route("/:boardIdx/comment")
  // 로그인 확인 x
  .get(validate({ params: BoardIdxParamReq, query: PagingReq }), getComment)
  .post(validate({ params: BoardIdxParamReq, body: UpsertCommentReq }), getUserInfo, addComment);

/**
 * 댓글 수정, 삭제
 */
route
  .route("/comment/:commentIdx")
  // 대댓글~ 조회
  .get(validate({ params: CommentIdxParamReq, query: PagingReq }), getCommentReplies)
  // 본인 확인은 db 조회가 필요하기에 서비스 내에서
  .put(validate({ params: CommentIdxParamReq, body: UpsertCommentReq }), getUserInfo, updateComment)
  .delete(validate({ params: CommentIdxParamReq }), getUserIdx, deleteComment);

export default route;
