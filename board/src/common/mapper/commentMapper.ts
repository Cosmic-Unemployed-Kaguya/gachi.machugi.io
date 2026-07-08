import { BoardCommentEntity } from "@domain/board-comment/boardCommentEntity";
import { QuizCommentEntity } from "@domain/quiz-comment/quizCommentEntity";

import { CommentListRes } from "@dto/commentListRes";
import { CommentRes } from "@dto/commentRes";
import { Page } from "@dto/paging";

import {
  GrpcCommentPageResponse,
  GrpcCommentResponse,
} from "@generated/machugi/board/common";

export const toCommentListRes = (
  entity: BoardCommentEntity | QuizCommentEntity,
  userNicknameMap: Record<number, string>,
): CommentListRes => {
  return {
    idx: entity.idx,
    parent: entity.parent ? entity.parent.idx : null,
    userNickName: userNicknameMap[entity.userIdx],
    content: entity.content,
    state: entity.state,
    updatedAt: entity.updatedAt,
  };
};

export const toCommentRes = (
  entity: BoardCommentEntity | QuizCommentEntity,
  userNickName: string,
): CommentRes => {
  return {
    idx: entity.idx,
    parent: entity.parent ? entity.parent.idx : null,
    userNickName: userNickName,
    content: entity.content,
    state: entity.state,
    updatedAt: entity.updatedAt,
  };
};

export const toGrpcCommentRes = (data: CommentRes): GrpcCommentResponse => {
  return {
    content: data.content,
    idx: data.idx,
    state: data.state,
    updatedAt: data.updatedAt,
    userNickName: data.userNickName,
    parent: data.parent ? data.parent : undefined,
  };
};

export const toGrpcCommentPageRes = (
  pagingData: Page<CommentListRes>,
): GrpcCommentPageResponse => {
  return {
    items: pagingData.items.map((item): GrpcCommentResponse => ({
      content: item.content,
      idx: item.idx,
      state: item.state,
      updatedAt: item.updatedAt,
      userNickName: item.userNickName,
      parent: item.parent ? item.parent : undefined,
    })),
    currentPage: pagingData.currentPage,
    hasNext: pagingData.hasNext,
    totalCount: pagingData.totalCount,
    totalPages: pagingData.totalPages,
  };
};
