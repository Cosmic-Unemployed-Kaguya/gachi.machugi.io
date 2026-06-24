import { Inject } from "typedi";
import { GrpcServer } from "../decorator/grpcServer";
import { BoardCommentGrpcServiceService as BoardCommentGrpcService, GrpcCommentIdxRequest, GrpcUpsertCommentRequest} from "../generated/board_comment";
import BoardCommentService from "../service/boardCommentService";
import UserClient from "../grpc-client/userClient";
import { GrpcCommentPageResponse, GrpcCommentResponse, GrpcPagingRequest } from "../generated/common";
import { toGrpcCommentPageRes, toGrpcCommentRes } from "../utils/mapper/commentMapper";
import { grpcValidate } from "../utils/grpcValidate";

import {UpsertCommentReq } from "../model/dto/commentUpsertReq"
import { BoardIdxParamReq , CommentIdxParamReq } from "../model/dto/idxParamReq"
import { PagingReq  } from "../model/dto/paging"
import { GrpcAuth } from "../decorator/grpcAuth";
import { AuthRequest } from "../model/dto/grpcBaseReq";
import { UserData } from "../middlewares/appRequest";
import { toUserData } from "../utils/mapper/userMapper";

@GrpcServer(BoardCommentGrpcService)
export default class BoardGrpcServer {

    constructor(
        @Inject() private boardCommentService :BoardCommentService,
        @Inject() private userClient :UserClient, 
    ){}

    
    public async getComment(req: GrpcPagingRequest) : Promise<GrpcCommentPageResponse>{
        
        const boardIdxReq = await grpcValidate(BoardIdxParamReq, req);
        const pagingReq = await grpcValidate(PagingReq, req);

        const data = await this.boardCommentService.getCommentPage(boardIdxReq, pagingReq);

        const res : GrpcCommentPageResponse = toGrpcCommentPageRes(data);

        return res;

    }

    @GrpcAuth() // 권한 검증 필요 x
    public async addComment(req : AuthRequest<GrpcUpsertCommentRequest> ) : Promise<GrpcCommentResponse>{

        // 요청 데이터 유효성 검사
        const boardIdxReq = await grpcValidate(BoardIdxParamReq ,req) 
        const upsertCommentReq = await grpcValidate(UpsertCommentReq, req)

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 추가
        const data = await this.boardCommentService.addComment(userData, boardIdxReq, upsertCommentReq)
        
        // 반환 데이터 생성
        const res : GrpcCommentResponse = toGrpcCommentRes(data);
        return res;

        
    }

    public async getCommentReplies(req: GrpcPagingRequest) : Promise<GrpcCommentPageResponse>{
        
        const commentIdxReq  = await grpcValidate(CommentIdxParamReq,req);
        const pagingReq = await grpcValidate(PagingReq, req);

        const data = await this.boardCommentService.getCommentRepliesPage(commentIdxReq, pagingReq);

        const res : GrpcCommentPageResponse = toGrpcCommentPageRes(data);
        
        return res;

    }
    @GrpcAuth() // 권한 검증 필요 x
    public async updateComment(req: AuthRequest<GrpcUpsertCommentRequest>) : Promise<GrpcCommentResponse>{
        
        const commentIdxReq  = await grpcValidate(CommentIdxParamReq,req);
        const upsertCommentReq = await grpcValidate(UpsertCommentReq, req);

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 수정
        const data = await this.boardCommentService.updateComment(userData, commentIdxReq, upsertCommentReq);

        // 단일 댓글 반환 데이터 생성
        const res : GrpcCommentResponse = toGrpcCommentRes(data);

        return res;


    }

    @GrpcAuth() // 권한 검증 필요 x
    public async deleteComment(req: AuthRequest<GrpcCommentIdxRequest>) : Promise<GrpcCommentIdxRequest>{
     
        const commentIdxReq  = await grpcValidate(CommentIdxParamReq,req);

        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        const data = await this.boardCommentService.deleteComment(userData, commentIdxReq);

        // commentIdx 하나 만 반환하기에 굳이 매퍼 안써도 된다 판단
        const res : GrpcCommentIdxRequest = data;

        return res;
        
    }

}