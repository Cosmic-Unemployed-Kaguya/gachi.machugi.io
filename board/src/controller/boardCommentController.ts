import Container from "typedi";
import { AppRequest, UserData } from "../middlewares/appRequest";
import { BoardIdxParamReqType, CommentIdxParamReqType } from "../model/dto/IdxParamReq";
import { catchAsync } from "../utils/catchAsync";
import CommentService from "../service/boardCommentService";
import {PagingReqType } from "../model/dto/paging";
import { sendSuccess } from "../model/dto/baseRes";
import { Response } from "express";
import { UpsertCommentReqType } from "../model/dto/upsertCommentReq";
import logger from "../utils/logger";


export const getComment =  catchAsync(async(req :AppRequest, res : Response) =>{
    
    const boardIdxReq : BoardIdxParamReqType = req.paramsData;

    const pageReq : PagingReqType = req.queryData;

    const commentService = Container.get(CommentService);

    const data = await commentService.getCommentPage(boardIdxReq,pageReq);

    return sendSuccess(res,data);

});

export const getCommentReplies =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const pageReq : PagingReqType = req.queryData;

    const commentService = Container.get(CommentService);

    const data = await commentService.getCommentRepliesPage(commentIdxReq,pageReq);

    return sendSuccess(res,data);

});


export const addComment =  catchAsync(async(req :AppRequest, res : Response) =>{

    const boardIdxReq : BoardIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const upsertCommentReq : UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(CommentService);

    const data = await commentService.addComment(userData, boardIdxReq, upsertCommentReq);

    return sendSuccess(res,data);

});

export const updateComment =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const upsertCommentReq : UpsertCommentReqType = req.bodyData;

    const commentService = Container.get(CommentService);

    const data = await commentService.updateComment(userData, commentIdxReq, upsertCommentReq);

    return sendSuccess(res,data);


});

export const deleteComment =  catchAsync(async(req :AppRequest, res : Response) =>{

    const commentIdxReq : CommentIdxParamReqType = req.paramsData;

    const userData : UserData  = req.userData!;

    const commentService = Container.get(CommentService);

    const data = await commentService.deleteComment(userData,commentIdxReq);

    return sendSuccess(res,data);
});

