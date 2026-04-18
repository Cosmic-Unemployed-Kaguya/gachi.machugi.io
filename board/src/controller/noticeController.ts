import { Request, Response } from "express";
import Container from "typedi";
import { Page, PagingReqType } from "../model/dto/paging";
import NoticeService from "../service/noticeService";
import { NoticeListRes } from "../model/dto/noticeListDTO";
import { sendSuccess } from "../model/dto/baseRes";
import { UpsertNoticeType } from '../model/dto/upsertNoticeReq';
import { AppRequest } from "../middlewares/appRequest";
import { catchAsync } from "../utils/catchAsync";
import { IdxParamReqType } from "../model/dto/IdxParamReq";

export const getNoticeList = catchAsync(async(req :AppRequest, res : Response) =>{

    // 요청 데이터 타입 지정 (validate로 검사를 하고 넘어왔기에 가능)
    // as unknown 을 거치는 이유 : TS가 깐깐해서 query의 타입은 PagingType이 아니라고 경고문 뱉는데
    // as unknown로 최면 걸어서 '너 지금부터 query 타입 아니야 ㅋㅋ' 라고 타락시킨다고 함
    // < Gemini 피셜, 이거 이상해...

    /**  이거 굉장히 마음에 안드는데 나중에 고쳐보자*/
    //const pagingReq = req.query as unknown as PagingReqType;

    // 고침!
    const pagingReq : PagingReqType = req.queryData;

    // DI 컨테이너에서 service를 꺼냄
    // >  controller가 class가 아닌 이상 의존성 주입이고 뭐시고 컨테이너에서 직접 꺼내 써야함..
    // >> 근데 controller를 class로 만들고 데코레이터까지 달고싶었어!!! >> nodejs 왜씀? nestjs 쓰면 되는거
    // >>> 그런 의미에서 nodejs를 공부하고 있는 지금은 일단 이렇게 하자....
    // >>>> 사실 DI컨테이너 쓰고 service랑 repository를 클래스로 만들고 데코레이터까지 달아놓은 시점에서 의미가 있나?ㅋㅋ
    const noticeService = Container.get(NoticeService);

    // 데이터 조회 및 반환
    const data :Page<NoticeListRes> = await noticeService.getNoticeList(pagingReq);

    return sendSuccess(res,data);

});

export const addNotice = catchAsync(async(req :AppRequest, res : Response) =>{
    const upsertNoticeReq :  UpsertNoticeType = req.bodyData!;
    
    const userIDX : number = req.userData!.userIdx;

    const noticeService = Container.get(NoticeService);

    // 데이터 저장 및 반환
    const data  = await noticeService.addNotice(userIDX , upsertNoticeReq)

    return sendSuccess(res,data);
});


export const getNoticeDetail = catchAsync(async(req :AppRequest, res : Response) =>{
    const idxParamReqType : IdxParamReqType = req.paramsData;
    
    const noticeService = Container.get(NoticeService);

    const data = await noticeService.getNoticeDetail(idxParamReqType);

    return sendSuccess(res,data);
});


export const updateNotice = catchAsync(async(req :AppRequest, res : Response) =>{

    const upsertNoticeBody :  UpsertNoticeType = req.bodyData;

    const idxParamReqType : IdxParamReqType = req.paramsData;

    // const userIDX : number = req.userData!.userIdx;

    const noticeService = Container.get(NoticeService);

    const data = await noticeService.updateNotice(upsertNoticeBody, idxParamReqType);

    return sendSuccess(res, data);

});


export const deleteNotice = async(req :AppRequest, res : Response) =>{
    
    const idxParamReqType : IdxParamReqType = req.paramsData;

    const noticeService = Container.get(NoticeService);

    const data = await noticeService.deleteNotice(idxParamReqType);

    return sendSuccess(res,data);
    
}