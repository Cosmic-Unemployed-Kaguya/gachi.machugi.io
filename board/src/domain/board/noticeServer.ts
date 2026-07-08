import { Inject } from "typedi";

import NoticeService from "./noticeService";

import UserClient from "@common/grpc-client/userClient";
import {
  toGrpcNoticeDetail,
  toGrpcNoticePage,
} from "@common/mapper/noticeMapper";
import { grpcValidate } from "@common/utils/grpcValidate";

import { AuthRequest } from "@dto/grpcBaseReq";
import { BoardIdxParamReq } from "@dto/idxParamReq";
import { UpsertNoticeReq } from "@dto/noticeUpsertReq";
import { PagingReq } from "@dto/paging";

import { GrpcAuth } from "@decorator/grpcAuth";
import { GrpcServer } from "@decorator/grpcServer";

import { GrpcPagingRequest } from "@generated/machugi/board/common";
import {
  GrpcBoardIdxRequest,
  GrpcNoticeDetailResponse,
  GrpcNoticePageResponse,
  GrpcUpsertNoticeRequest,
  NoticeGrpcServiceService as NoticeGrpcService,
} from "@generated/machugi/board/notice";
import { UserRole } from "@generated/machugi/board/user";

@GrpcServer(NoticeGrpcService)
export default class NoticeGrpcServer {
  constructor(
    @Inject() private noticeService: NoticeService,
    @Inject() private userClient: UserClient,
  ) {}

  public async getNoticeList(
    req: GrpcPagingRequest,
  ): Promise<GrpcNoticePageResponse> {
    // 1. 요청 데이터 유효성 검사
    const pagingReq = await grpcValidate(PagingReq, req);

    // 2. 데이터 조회
    const data = await this.noticeService.getNoticeList(pagingReq);

    // 3. 반환데이터 생성
    const res: GrpcNoticePageResponse = toGrpcNoticePage(data);

    return res;
  }

  @GrpcAuth([UserRole.ADMIN])
  public async addNotice(
    req: AuthRequest<GrpcUpsertNoticeRequest>,
  ): Promise<GrpcNoticeDetailResponse> {
    // 1. 요청 데이터 유효성 검사
    const upsertNoticeReq = await grpcValidate(UpsertNoticeReq, req);

    // 2. user idx 가져오기(GrpcAuth에서 넣어줌)
    const userIdx: number = req.userData!.userIdx;

    // 3. 데이터 저장
    const data = await this.noticeService.addNotice(userIdx, upsertNoticeReq);

    // 4. 반환 데이터 생성
    const res: GrpcNoticeDetailResponse = toGrpcNoticeDetail(data);

    return res;
  }

  public async getNoticeDetail(
    req: GrpcBoardIdxRequest,
  ): Promise<GrpcNoticeDetailResponse> {
    // 1. 요청 데이터 유효성 검사
    const boardIdxReq = await grpcValidate(BoardIdxParamReq, req);

    // 2. 데이터 조회
    const data = await this.noticeService.getNoticeDetail(boardIdxReq);

    // 3. 반환 데이터 생성
    const res: GrpcNoticeDetailResponse = toGrpcNoticeDetail(data);

    return res;
  }

  @GrpcAuth([UserRole.ADMIN])
  public async updateNotice(
    req: AuthRequest<GrpcUpsertNoticeRequest>,
  ): Promise<GrpcNoticeDetailResponse> {
    // 1. 요청 데이터 유효성 검사
    const upsertNoticeReq = await grpcValidate(UpsertNoticeReq, req);

    const boardIdxReq = await grpcValidate(BoardIdxParamReq, req);

    // 2. 데이터 수정
    const data = await this.noticeService.updateNotice(
      boardIdxReq,
      upsertNoticeReq,
    );

    // 3. 반환 데이터 생성
    const res: GrpcNoticeDetailResponse = toGrpcNoticeDetail(data);

    return res;
  }

  @GrpcAuth([UserRole.ADMIN])
  public async deleteNotice(
    req: AuthRequest<GrpcBoardIdxRequest>,
  ): Promise<GrpcNoticePageResponse> {
    const boardIdxReq = await grpcValidate(BoardIdxParamReq, req);

    const data = await this.noticeService.deleteNotice(boardIdxReq);

    const res: GrpcNoticePageResponse = toGrpcNoticePage(data);

    return res;
  }
}
