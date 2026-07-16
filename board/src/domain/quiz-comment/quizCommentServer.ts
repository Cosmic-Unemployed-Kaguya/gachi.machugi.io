import { Inject } from "typedi";

import QuizCommentService from "./quizCommentService";

import UserClient from "@common/grpc-client/userClient";
import {
  toGrpcCommentPageRes,
  toGrpcCommentRes,
} from "@common/mapper/commentMapper";
import { toUserData } from "@common/mapper/userMapper";
import { UserData } from "@common/middlewares/appRequest";
import { grpcValidate } from "@common/utils/grpcValidate";

import { UpsertCommentReq } from "@dto/commentUpsertReq";
import { AuthRequest } from "@dto/grpcBaseReq";
import { CommentIdxParamReq, QuizIdxParamReq } from "@dto/idxParamReq";
import { PagingReq } from "@dto/paging";

import { GrpcAuth } from "@decorator/grpcAuth";
import { GrpcServer } from "@decorator/grpcServer";

import { GrpcCommentIdxRequest } from "@generated/machugi/board/board_comment";
import {
  GrpcCommentPageResponse,
  GrpcCommentResponse,
} from "@generated/machugi/board/common";
import {
  GrpcPagingQuizCommentRequest,
  GrpcQuizCommentIdxRequest,
  GrpcUpsertQuizCommentRequest,
  QuizCommentGrpcServiceService as QuizCommentGrpcService,
} from "@generated/machugi/board/quiz_comment";

@GrpcServer(QuizCommentGrpcService)
export default class QuizCommentGrpcServer {
  constructor(
    @Inject() private quizCommentService: QuizCommentService,
    @Inject() private userClient: UserClient,
  ) {}

  public async getQuizComment(
    req: GrpcPagingQuizCommentRequest,
  ): Promise<GrpcCommentPageResponse> {
    const quizIdxReq = await grpcValidate(QuizIdxParamReq, req);
    const pagingReq = await grpcValidate(PagingReq, req);

    const data = await this.quizCommentService.getCommentPage(
      quizIdxReq,
      pagingReq,
    );

    const res: GrpcCommentPageResponse = toGrpcCommentPageRes(data);

    return res;
  }

  @GrpcAuth()
  public async addQuizComment(
    req: AuthRequest<GrpcUpsertQuizCommentRequest>,
  ): Promise<GrpcCommentResponse> {
    // 요청 데이터 유효성 검사
    const quizIdxReq = await grpcValidate(QuizIdxParamReq, req);
    const upsertCommentReq = await grpcValidate(UpsertCommentReq, req);

    // 유저 데이터 가져오기
    const userRes = await this.userClient.getUserInfo({
      userIdx: req.userData!.userIdx,
    });

    const userData: UserData = toUserData(userRes);

    // 데이터 추가
    const data = await this.quizCommentService.addComment(
      userData,
      quizIdxReq,
      upsertCommentReq,
    );

    // 반환 데이터 생성
    const res: GrpcCommentResponse = toGrpcCommentRes(data);
    return res;
  }

  public async getQuizCommentReplies(
    req: GrpcPagingQuizCommentRequest,
  ): Promise<GrpcCommentPageResponse> {
    const commentIdxReq = await grpcValidate(CommentIdxParamReq, req);
    const pagingReq = await grpcValidate(PagingReq, req);

    const data = await this.quizCommentService.getCommentRepliesPage(
      commentIdxReq,
      pagingReq,
    );

    const res: GrpcCommentPageResponse = toGrpcCommentPageRes(data);

    return res;
  }

  @GrpcAuth()
  public async updateQuizComment(
    req: AuthRequest<GrpcUpsertQuizCommentRequest>,
  ): Promise<GrpcCommentResponse> {
    const commentIdxReq = await grpcValidate(CommentIdxParamReq, req);
    const upsertCommentReq = await grpcValidate(UpsertCommentReq, req);

    // 유저 데이터 가져오기
    const userRes = await this.userClient.getUserInfo({
      userIdx: req.userData!.userIdx,
    });

    const userData: UserData = toUserData(userRes);

    // 데이터 수정
    const data = await this.quizCommentService.updateComment(
      userData,
      commentIdxReq,
      upsertCommentReq,
    );

    // 단일 댓글 반환 데이터 생성
    const res: GrpcCommentResponse = toGrpcCommentRes(data);

    return res;
  }

  @GrpcAuth()
  public async deleteQuizComment(
    req: AuthRequest<GrpcQuizCommentIdxRequest>,
  ): Promise<GrpcCommentIdxRequest> {
    const commentIdxReq = await grpcValidate(CommentIdxParamReq, req);

    const userRes = await this.userClient.getUserInfo({
      userIdx: req.userData!.userIdx,
    });

    const userData: UserData = toUserData(userRes);

    const data = await this.quizCommentService.deleteComment(
      userData,
      commentIdxReq,
    );

    // commentIdx 하나 만 반환하기에 굳이 매퍼 안써도 된다 판단
    const res: GrpcCommentIdxRequest = data;

    return res;
  }
}
