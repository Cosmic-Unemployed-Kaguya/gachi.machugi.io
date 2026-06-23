import { Request, Response } from "express";
import Container from "typedi";
import { Page, PagingReqType } from "../model/dto/paging";
import NoticeService from "../service/noticeService";
import { NoticeListRes } from "../model/dto/noticeListDTO";
import { sendSuccess } from "../model/dto/baseRes";
import { UpsertNoticeType } from '../model/dto/noticeUpsertReq';
import { AppRequest } from "../middlewares/appRequest";
import { catchAsync } from "../utils/catchAsync";
import { BoardIdxParamReqType } from "../model/dto/idxParamReq";

export const getNoticeList = catchAsync(async(req :AppRequest, res : Response) =>{

    const pagingReq : PagingReqType = req.queryData;

    const noticeService = Container.get(NoticeService);

    const data :Page<NoticeListRes> = await noticeService.getNoticeList(pagingReq);

    return sendSuccess(res,data);

});

export const addNotice = catchAsync(async(req :AppRequest, res : Response) =>{

    const upsertNoticeReq :  UpsertNoticeType = req.bodyData!;
    
    const userIDX : number = req.userData!.userIdx;

    const noticeService = Container.get(NoticeService);

    const data  = await noticeService.addNotice(userIDX , upsertNoticeReq)

    return sendSuccess(res,data);
});


export const getNoticeDetail = catchAsync(async(req :AppRequest, res : Response) =>{
    const boardIdxParamReq : BoardIdxParamReqType = req.paramsData;
    
    const noticeService = Container.get(NoticeService);

    const data = await noticeService.getNoticeDetail(boardIdxParamReq);

    return sendSuccess(res,data);
});


export const updateNotice = catchAsync(async(req :AppRequest, res : Response) =>{

    const upsertNoticeBody :  UpsertNoticeType = req.bodyData;

    const boardIdxParamReq : BoardIdxParamReqType = req.paramsData;

    const noticeService = Container.get(NoticeService);

    const data = await noticeService.updateNotice( boardIdxParamReq, upsertNoticeBody);

    return sendSuccess(res, data);

});


export const deleteNotice = async(req :AppRequest, res : Response) =>{
    
    const boardIdxParamReq : BoardIdxParamReqType = req.paramsData;

    const noticeService = Container.get(NoticeService);

    const data = await noticeService.deleteNotice(boardIdxParamReq);

    return sendSuccess(res,data);
    
}