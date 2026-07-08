import { Response } from "express";
import Container from "typedi";

import QuizCommentService from "./quizCommentService";

import { AppRequest, UserData } from "@common/middlewares/appRequest";
import { catchAsync } from "@common/middlewares/catchAsync";

import { sendSuccess } from "@dto/baseRes";
import { UpsertCommentReqType } from "@dto/commentUpsertReq";
import { CommentIdxParamReqType, QuizIdxParamReqType } from "@dto/idxParamReq";
import { PagingReqType } from "@dto/paging";

export const getQuizComment = catchAsync(
  async (req: AppRequest, res: Response) => {
    const quizIdxReq: QuizIdxParamReqType = req.paramsData;

    const pageReq: PagingReqType = req.queryData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.getCommentPage(quizIdxReq, pageReq);

    return sendSuccess(res, data);
  },
);

export const getQuizCommentReplies = catchAsync(
  async (req: AppRequest, res: Response) => {
    const commentIdxReq: CommentIdxParamReqType = req.paramsData;

    const pageReq: PagingReqType = req.queryData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.getCommentRepliesPage(
      commentIdxReq,
      pageReq,
    );

    return sendSuccess(res, data);
  },
);

export const addQuizComment = catchAsync(
  async (req: AppRequest, res: Response) => {
    const quizIdxReq: QuizIdxParamReqType = req.paramsData;

    const userData: UserData = req.userData!;

    const upsertCommentReq: UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.addComment(
      userData,
      quizIdxReq,
      upsertCommentReq,
    );

    return sendSuccess(res, data);
  },
);

export const updateQuizComment = catchAsync(
  async (req: AppRequest, res: Response) => {
    const commentIdxReq: CommentIdxParamReqType = req.paramsData;

    const userData: UserData = req.userData!;

    const upsertCommentReq: UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.updateComment(
      userData,
      commentIdxReq,
      upsertCommentReq,
    );

    return sendSuccess(res, data);
  },
);

export const deleteQuizComment = catchAsync(
  async (req: AppRequest, res: Response) => {
    const commentIdxReq: CommentIdxParamReqType = req.paramsData;

    const userData: UserData = req.userData!;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.deleteComment(userData, commentIdxReq);

    return sendSuccess(res, data);
  },
);
