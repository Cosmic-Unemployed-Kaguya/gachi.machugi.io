import { Response } from "express";
import Container from "typedi";

import CommentService from "./boardCommentService";

import { AppRequest, UserData } from "@common/middlewares/appRequest";
import { catchAsync } from "@common/middlewares/catchAsync";

import { sendSuccess } from "@dto/baseRes";
import { UpsertCommentReqType } from "@dto/commentUpsertReq";
import { BoardIdxParamReqType, CommentIdxParamReqType } from "@dto/idxParamReq";
import { PagingReqType } from "@dto/paging";

export const getComment = catchAsync(async (req: AppRequest, res: Response) => {
  const boardIdxReq: BoardIdxParamReqType = req.paramsData;

  const pageReq: PagingReqType = req.queryData;

  const commentService = Container.get(CommentService);

  const data = await commentService.getCommentPage(boardIdxReq, pageReq);

  return sendSuccess(res, data);
});

export const getCommentReplies = catchAsync(async (req: AppRequest, res: Response) => {
  const commentIdxReq: CommentIdxParamReqType = req.paramsData;

  const pageReq: PagingReqType = req.queryData;

  const commentService = Container.get(CommentService);

  const data = await commentService.getCommentRepliesPage(commentIdxReq, pageReq);

  return sendSuccess(res, data);
});

export const addComment = catchAsync(async (req: AppRequest, res: Response) => {
  const boardIdxReq: BoardIdxParamReqType = req.paramsData;

  const userData: UserData = req.userData!;

  const upsertCommentReq: UpsertCommentReqType = req.bodyData;

  const commentService = Container.get(CommentService);

  const data = await commentService.addComment(userData, boardIdxReq, upsertCommentReq);

  return sendSuccess(res, data);
});

export const updateComment = catchAsync(async (req: AppRequest, res: Response) => {
  const commentIdxReq: CommentIdxParamReqType = req.paramsData;

  const userData: UserData = req.userData!;

  const upsertCommentReq: UpsertCommentReqType = req.bodyData;

  const commentService = Container.get(CommentService);

  const data = await commentService.updateComment(userData, commentIdxReq, upsertCommentReq);

  return sendSuccess(res, data);
});

export const deleteComment = catchAsync(async (req: AppRequest, res: Response) => {
  const commentIdxReq: CommentIdxParamReqType = req.paramsData;

  const userData: UserData = req.userData!;

  const commentService = Container.get(CommentService);

  const data = await commentService.deleteComment(userData, commentIdxReq);

  return sendSuccess(res, data);
});
