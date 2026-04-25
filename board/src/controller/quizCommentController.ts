import Container from "typedi";
import { AppRequest, UserData } from "../middlewares/appRequest";
import { CommentIdxParamReqType, QuizIdxParamReqType } from "../model/dto/idxParamReq";
import { PagingReqType } from "../model/dto/paging";
import { catchAsync } from "../utils/catchAsync";
import { Response } from "express";
import QuizCommentService from "../service/quizCommentService"
import { sendSuccess } from "../model/dto/baseRes";
import { UpsertCommentReqType } from "../model/dto/upsertCommentReq";


export const getQuizComment =  catchAsync(async(req :AppRequest, res : Response) =>{
    
    const quizIdxReq : QuizIdxParamReqType = req.paramsData;

    const pageReq : PagingReqType = req.queryData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.getCommentPage(quizIdxReq,pageReq);

    return sendSuccess(res,data);

});

export const getQuizCommentReplies =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const pageReq : PagingReqType = req.queryData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.getCommentRepliesPage(commentIdxReq,pageReq);

    return sendSuccess(res,data);


});


export const addQuizComment =  catchAsync(async(req :AppRequest, res : Response) =>{


    const quizIdxReq : QuizIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const upsertCommentReq : UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.addComment(userData, quizIdxReq, upsertCommentReq);

    return sendSuccess(res,data);
});

export const updateQuizComment =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const upsertCommentReq : UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.updateComment(userData, commentIdxReq, upsertCommentReq);

    return sendSuccess(res,data);



});

export const deleteQuizComment =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const commentService = Container.get(QuizCommentService);

    const data = await commentService.deleteComment(userData,commentIdxReq);

    return sendSuccess(res,data);

});

