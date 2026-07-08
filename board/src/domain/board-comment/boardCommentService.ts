import { Inject, Service } from "typedi";

import { BoardCommentEntity } from "./boardCommentEntity";
import { BoardCommentRepository } from "./boardCommentRepository";

import { ForbiddenError } from "@common/error-handler/error";
import UserClient from "@common/grpc-client/userClient";
import { toCommentListRes, toCommentRes } from "@common/mapper/commentMapper";
import { toPageDTO } from "@common/mapper/pageMapper";
import { UserData } from "@common/middlewares/appRequest";

import { CommentDeleteRes } from "@dto/commentDeleteRes";
import { CommentListRes } from "@dto/commentListRes";
import { CommentRes } from "@dto/commentRes";
import { UpsertCommentReqType } from "@dto/commentUpsertReq";
import { BoardIdxParamReqType, CommentIdxParamReqType } from "@dto/idxParamReq";
import { Page, PagingReqType } from "@dto/paging";

import { UserInfoListResponse } from "@generated/machugi/board/user";

@Service()
export default class BoardCommentService {
  constructor(
    @Inject() private boardCommentRepository: BoardCommentRepository,
    @Inject() private userClient: UserClient,
  ) {}

  /**
   * 최상위 댓글 리스트 조회 (페이징)
   * @param boardIdx
   * @param pagingReq
   * @returns Page<CommentListRes>
   */
  public async getCommentPage(
    boardIdx: BoardIdxParamReqType,
    pagingReq: PagingReqType,
  ): Promise<Page<CommentListRes>> {
    // 1. 댓글 데이터 조회
    const pagingEntity: Page<BoardCommentEntity> =
      await this.boardCommentRepository.findCommentByPaging(
        boardIdx.boardIdx,
        pagingReq,
      );

    // 2. 댓글 작성자들 nickname 조회
    const commenters: number[] = pagingEntity.items.map(
      (entity) => entity.userIdx,
    );

    const usersRes: UserInfoListResponse =
      await this.userClient.getUserListInfo({ userIdxs: commenters });

    // 3. idx와 닉네임을 매핑하여 dto에 추가
    // 3.1 <idx: nickname> 형태로 매핑
    const userNicknameMap: Record<number, string> = usersRes.users.reduce<
      Record<number, string>
    >((acc, user) => {
      acc[user.userIdx] = user.nickName;
      return acc;
    }, {});

    // 4. 반환 데이터 생성
    const pageDTO: Page<CommentListRes> = toPageDTO(pagingEntity, (entity) =>
      toCommentListRes(entity, userNicknameMap),
    );
    return pageDTO;
  }

  /**
   * 댓글 추가
   * @param userIdx
   * @param boardIdx
   * @param upsertCommnetReq
   * @returns 댓글 리스트 반환
   */
  public async addComment(
    userData: UserData,
    boardIdx: BoardIdxParamReqType,
    upsertCommnetReq: UpsertCommentReqType,
  ): Promise<CommentRes> {
    // 1. 새로운 entitiy 생성
    const comment: BoardCommentEntity = this.boardCommentRepository.create({
      board: { idx: boardIdx.boardIdx },
      content: upsertCommnetReq.content,
      state: upsertCommnetReq.state,
      userIdx: userData.userIdx,
      parent: upsertCommnetReq.parent
        ? { idx: upsertCommnetReq.parent }
        : undefined,
    });

    // 2. 저장
    const savedComment = await this.boardCommentRepository.save(comment);

    // 3. 닉네임 가져오기.

    // const userNickName = userData.userNickName ? userData.userNickName : "알 수 없음"  // 아래 코드로 단축
    const userNickName = userData.userNickName || "알 수 없음";

    // 4. 반환 데이터 생성
    return toCommentRes(savedComment, userNickName);
  }

  /**
   * 대댓글 조회
   * @param commentIdx
   * @param pagingReq
   * @returns Page<CommentListRes>
   */
  public async getCommentRepliesPage(
    commentIdx: CommentIdxParamReqType,
    pagingReq: PagingReqType,
  ): Promise<Page<CommentListRes>> {
    // 1. 해당 댓글(commentIdx)의 대댓글 데이터 조회
    const pagingEntity: Page<BoardCommentEntity> =
      await this.boardCommentRepository.findCommentRepliesByPaging(
        commentIdx.commentIdx,
        pagingReq,
      );

    // 2. 댓글 작성자들 nickname 조회
    const commenters: number[] = pagingEntity.items.map(
      (entity) => entity.userIdx,
    );

    const usersRes: UserInfoListResponse =
      await this.userClient.getUserListInfo({ userIdxs: commenters });

    // 3. idx와 닉네임을 매핑하여 dto에 추가
    // 3.1 <idx: nickname> 형태로 매핑
    const userNicknameMap: Record<number, string> = usersRes.users.reduce<
      Record<number, string>
    >((acc, user) => {
      acc[user.userIdx] = user.nickName;
      return acc;
    }, {});

    // 4. 반환 데이터 생성
    const pageDTO: Page<CommentListRes> = toPageDTO(pagingEntity, (entity) =>
      toCommentListRes(entity, userNicknameMap),
    );
    return pageDTO;
  }

  /**
   * 댓글 수정
   * @param userData
   * @param commentIdxReq
   * @param upsertCommnetReq
   * @returns CommentRes
   */
  public async updateComment(
    userData: UserData,
    commentIdxReq: CommentIdxParamReqType,
    upsertCommnetReq: UpsertCommentReqType,
  ): Promise<CommentRes> {
    // 1. db 조회, 없을 시 에러
    const comment = await this.boardCommentRepository.findOneByOrFail({
      idx: commentIdxReq.commentIdx,
    });

    // 2. 본인 확인!!!
    if (comment.userIdx !== userData.userIdx) {
      throw new ForbiddenError("댓글 작성자만 수정 가능합니다");
    }

    // 3. entity 수정 , 부모에 대한 수정은 X >  실질적으로 대댓글을 수정 할 때 상위 댓글이 뭐인지를 바꾸진 않지..?
    comment.update(upsertCommnetReq.content, upsertCommnetReq.state);

    // 4. db 수정
    const updatedComment = await this.boardCommentRepository.save(comment);

    // 5. 닉네임 가져오기
    // const userNickName = userData.userNickName ? userData.userNickName : "알 수 없음"  // 아래 코드로 단축
    const userNickName = userData.userNickName || "알 수 없음";

    // 6. 반환 데이터 생성
    return toCommentRes(updatedComment, userNickName);
  }

  /**
   * 댓글 삭제
   * @param userData
   * @param commentIdxReq
   * @returns  commentIdx
   */
  public async deleteComment(
    userData: UserData,
    commentIdxReq: CommentIdxParamReqType,
  ): Promise<CommentDeleteRes> {
    // 1. db 조회, 없을 시 에러
    const comment = await this.boardCommentRepository.findOneByOrFail({
      idx: commentIdxReq.commentIdx,
    });

    // 2. 본인 확인
    if (comment.userIdx !== userData.userIdx) {
      throw new ForbiddenError("댓글 작성자만 삭제 가능합니다");
    }

    // 3. soft delete
    await this.boardCommentRepository.softRemove(comment);

    // 4. 반환. 지금은 idx 만?
    return { commentIdx: commentIdxReq.commentIdx };
  }
}
