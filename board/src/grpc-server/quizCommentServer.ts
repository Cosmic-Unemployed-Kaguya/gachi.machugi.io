import { Inject } from "typedi";
import { GrpcServer } from "../decorator/grpcServer";
import { PagingQuizCommentReq, QuizCommentApiServer, QuizCommentApiService, QuizCommentIdxReq, UpsertQuizCommentReq } from "../generated/quizComment";
import QuizCommentService from "../service/quizCommentService";
import UserClient from "../grpc-client/userClient";
import { handleUnaryCall } from "@grpc/grpc-js";
import { grpcValidate } from "../utils/grpcValidate";
import { QuizIdxParamReq as AppQuizIdxReq ,CommentIdxParamReq as AppCommentIdxReq } from "../model/dto/idxParamReq"
import { PagingReq as AppPagingReq } from '../model/dto/paging';
import {UpsertCommentReq as AppUpsertCommentReq} from "../model/dto/commentUpsertReq"
import { toGrpcCommentPageRes, toGrpcCommentRes } from "../utils/mapper/commentMapper";
import { CommentIdxReq, CommentPageRes, CommentRes } from "../generated/notice";
import { GrpcAuth } from "../decorator/grpcAuth";
import { AuthRequest } from "../model/dto/grpcBaseReq";
import { toUserData } from "../utils/mapper/userMapper";
import { UserData } from "../middlewares/appRequest";


@GrpcServer(QuizCommentApiService)
export default class QuizCommentGrpcServer{

    constructor(
        @Inject() private quizCommentService : QuizCommentService,
        @Inject() private userClient :UserClient,
    ){}


    public async getQuizComment(req : PagingQuizCommentReq):Promise<CommentPageRes>{
        const quizIdxReq = await grpcValidate(AppQuizIdxReq, req);
        const pagingReq = await grpcValidate(AppPagingReq, req);

        const data = await this.quizCommentService.getCommentPage(quizIdxReq, pagingReq);

        const res : CommentPageRes = toGrpcCommentPageRes(data);

        return res;
    }

    @GrpcAuth()
    public async addQuizComment(req :AuthRequest<UpsertQuizCommentReq>): Promise<CommentRes>{

        // 요청 데이터 유효성 검사
        const quizIdxReq = await grpcValidate(AppQuizIdxReq ,req) 
        const upsertCommentReq = await grpcValidate(AppUpsertCommentReq, req)

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 추가
        const data = await this.quizCommentService.addComment(userData, quizIdxReq, upsertCommentReq)
        
        // 반환 데이터 생성
        const res : CommentRes = toGrpcCommentRes(data);
        return res;


    }

    public async getQuizCommentReplies(req : PagingQuizCommentReq): Promise<CommentPageRes>{

        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);
        const pagingReq = await grpcValidate(AppPagingReq, req);

        const data = await this.quizCommentService.getCommentRepliesPage(commentIdxReq, pagingReq);

        const res : CommentPageRes = toGrpcCommentPageRes(data);
        
        return res;

    }

    @GrpcAuth()
    public async updateQuizComment(req :AuthRequest<UpsertQuizCommentReq>): Promise<CommentRes>{
        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);
        const upsertCommentReq = await grpcValidate(AppUpsertCommentReq, req);

        // 유저 데이터 가져오기
        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        // 데이터 수정
        const data = await this.quizCommentService.updateComment(userData, commentIdxReq, upsertCommentReq);

        // 단일 댓글 반환 데이터 생성
        const res = toGrpcCommentRes(data);

        return res;
    }


    @GrpcAuth()
    public async deleteQuizComment(req :AuthRequest<QuizCommentIdxReq>): Promise<CommentIdxReq>{
        const commentIdxReq  = await grpcValidate(AppCommentIdxReq,req);

        const userRes = await this.userClient.getUserInfo({userIdx : req.userData!.userIdx});

        const userData : UserData = toUserData(userRes);

        const data = await this.quizCommentService.deleteComment(userData, commentIdxReq);

        // commentIdx 하나 만 반환하기에 굳이 매퍼 안써도 된다 판단
        const res = data;

        return res;
        
    }



}
