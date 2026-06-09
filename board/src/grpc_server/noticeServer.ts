import { Inject } from "typedi";
import { GrpcServer } from "../decorator/grpcServer";
import { BoardIdxReq, CommentIdxReq, CommentPageRes, CommentRes, NoticeApiService, NoticeDetailRes, NoticePageRes, PagingReq, UpsertNoticeReq } from "../generated/notice";
import NoticeService from "../service/noticeService";
import BoardCommentService from "../service/boardCommentService";
import UserClient from "../client/userClient";
import { grpcValidate } from "../utils/grpcValidate";
import {UpsertNoticeReq  as AppUpsertNoticeReq} from "../model/dto/noticeUpsertReq"
import {UpsertCommentReq as AppUpsertCommentReq} from "../model/dto/commentUpsertReq"
import { BoardIdxParamReq as AppBoardIdxReq, CommentIdxParamReq as AppCommentIdxReq} from "../model/dto/idxParamReq"
import { PagingReq as AppPagingReq } from "../model/dto/paging"


import { UpsertCommentReq } from './../generated/notice';
import { GrpcAuth } from "../decorator/grpcAuth";
import { UserRole } from "../generated/user";
import { AuthRequest } from "../model/dto/grpcBaseReq";
import { UserData } from "../middlewares/appRequest";
import { toGrpcNoticeDetail, toGrpcNoticePage } from "../utils/mapper/noticeMapper";
import { toGrpcCommentPageRes, toGrpcCommentRes } from "../utils/mapper/commentMapper";
import { toUserData } from "../utils/mapper/userMapper";


@GrpcServer(NoticeApiService)
export default class NoticeGrpcServer   {

    constructor(
        @Inject() private noticeService : NoticeService,
        @Inject() private boardCommentService :BoardCommentService,
        @Inject() private userClient :UserClient,
    ){}

    public async getNoticeList(req: PagingReq) : Promise<NoticePageRes>{
        // 1. 요청 데이터 유효성 검사
        const pagingReq = await grpcValidate(AppPagingReq, req);

        // 2. 데이터 조회
        const data = await this.noticeService.getNoticeList(pagingReq);

        // 3. 반환데이터 생성
        const res : NoticePageRes = toGrpcNoticePage(data);

        return res;

    }

    @GrpcAuth([UserRole.ADMIN])
    public async addNotice(req: AuthRequest<UpsertNoticeReq>) : Promise<NoticeDetailRes>{
        
        // 1. 요청 데이터 유효성 검사
        const upsertNoticeReq = await grpcValidate(AppUpsertNoticeReq, req);

        // 2. user idx 가져오기(GrpcAuth에서 넣어줌)
        const userIdx :number = req.userData!.userIdx;

        // 3. 데이터 저장 
        const data = await this.noticeService.addNotice(userIdx, upsertNoticeReq);

        // 4. 반환 데이터 생성 
        const res :NoticeDetailRes = toGrpcNoticeDetail(data);

        return res;
    }

    public async getNoticeDetail(req: BoardIdxReq) : Promise<NoticeDetailRes>{
        // 1. 요청 데이터 유효성 검사
        const boardIdxReq = await grpcValidate(AppBoardIdxReq, req);

        // 2. 데이터 조회
        const data = await this.noticeService.getNoticeDetail(boardIdxReq);

        // 3. 반환 데이터 생성
        const res :NoticeDetailRes = toGrpcNoticeDetail(data);

        return res;
        
    }

    @GrpcAuth([UserRole.ADMIN])
    public async updateNotice(req: AuthRequest<UpsertNoticeReq>) : Promise<NoticeDetailRes>{
        
        // 1. 요청 데이터 유효성 검사
        const upsertNoticeReq = await grpcValidate(AppUpsertNoticeReq, req);

        const boardIdxReq = await grpcValidate(AppBoardIdxReq, req);

        // 2. 데이터 수정
        const data = await this.noticeService.updateNotice(boardIdxReq, upsertNoticeReq);

        // 3. 반환 데이터 생성
        const res :NoticeDetailRes = toGrpcNoticeDetail(data);

        return res;

    }

    @GrpcAuth([UserRole.ADMIN])
    public async deleteNotice(req: AuthRequest<BoardIdxReq>) : Promise<NoticePageRes>{
        
        const boardIdxReq = await grpcValidate(AppBoardIdxReq, req);

        const data  = await this.noticeService.deleteNotice(boardIdxReq);

        const res : NoticePageRes = toGrpcNoticePage(data);

        return res; 

    }

    public async getComment(req: PagingReq) : Promise<CommentPageRes>{
        
        const boardIdxReq = await grpcValidate(AppBoardIdxReq, req);
        const pagingReq = await grpcValidate(AppPagingReq, req);

        const data = await this.boardCommentService.getCommentPage(boardIdxReq, pagingReq);

        const res : CommentPageRes = toGrpcCommentPageRes(data);

        return res;

    }

    @GrpcAuth() // 권한 검증 필요 x
    public async addComment(req : AuthRequest<UpsertCommentReq> ) : Promise<CommentRes>{

        // 요청 데이터 유효성 검사
        const boardIdxReq = await grpcValidate(AppBoardIdxReq ,req) 
        const upsertCommentReq = await grpcValidate(AppUpsertCommentReq, req)

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 추가
        const data = await this.boardCommentService.addComment(userData, boardIdxReq, upsertCommentReq)
        
        // 반환 데이터 생성
        const res : CommentRes = toGrpcCommentRes(data);
        return res;

        
    }

    public async getCommentReplies(req: PagingReq) : Promise<CommentPageRes>{
        
        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);
        const pagingReq = await grpcValidate(AppPagingReq, req);

        const data = await this.boardCommentService.getCommentRepliesPage(commentIdxReq, pagingReq);

        const res : CommentPageRes = toGrpcCommentPageRes(data);
        
        return res;

    }
    @GrpcAuth() // 권한 검증 필요 x
    public async updateComment(req: AuthRequest<UpsertCommentReq>) : Promise<CommentRes>{
        
        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);
        const upsertCommentReq = await grpcValidate(AppUpsertCommentReq, req);

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 수정
        const data = await this.boardCommentService.updateComment(userData, commentIdxReq, upsertCommentReq);

        // 단일 댓글 반환 데이터 생성
        const res = toGrpcCommentRes(data);

        return res;


    }

    @GrpcAuth() // 권한 검증 필요 x
    public async deleteComment(req: AuthRequest<CommentIdxReq>) : Promise<CommentIdxReq>{
     
        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);

        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        const data = await this.boardCommentService.deleteComment(userData, commentIdxReq);

        // commentIdx 하나 만 반환하기에 굳이 매퍼 안써도 된다 판단
        const res = data;

        return res;
        
    }

}

