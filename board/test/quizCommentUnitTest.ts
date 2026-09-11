import { afterEach, beforeEach, describe, it, mock } from "node:test";

import assert from "assert";

import UserClient from "@common/grpc-client/userClient";
import { UserData } from "@common/middlewares/appRequest";
import { UpsertCommentReqType } from "@common/model/dto/commentUpsertReq";
import { PagingReqType } from "@common/model/dto/paging";

import { QuizCommentEntity } from "@common/model/entity/quizCommentEntity";
import { QuizCommentRepository } from "@domain/quiz-comment/quizCommentRepository";
import QuizCommentService from "@domain/quiz-comment/quizCommentService";

import { BoardState } from "@enum/boardState";
import { UserRole } from "@enum/userRole";

import { UserInfoResponse } from "@generated/machugi/board/user";

describe("QuizCommentService 단위 테스트, 성공 케이스", () => {
  let quizCommentService: QuizCommentService;
  let mockCommentRepo: QuizCommentRepository;
  let mockUserClient: UserClient;

  let mockEntityPage: any;
  let mockEntityReplyPage: any;
  let mockEntityDetail: any;
  let mockUserInfo: any;
  let mockUpsertCommentReq: any;

  beforeEach(() => {
    mockEntityPage = {
      items: [
        {
          idx: 1,
          state: BoardState.PUBLIC,
          userIdx: 1,
          content: "1빠",
          quiz_idx: 1,
          createdAt: new Date("2026-04-19T04:02:03.000Z"),
          updatedAt: new Date("2026-04-19T04:02:03.000Z"),
          deletedAt: null,
        } as unknown as QuizCommentEntity,
      ],
      totalCount: 1,
      totalPages: 1,
      currentPage: 1,
      hasNext: false,
    };

    mockEntityReplyPage = {
      items: [
        {
          idx: 2,
          state: BoardState.PUBLIC,
          userIdx: 1,
          content: "테스트 댓글 추가/수정 1",
          board_idx: 1,
          parent: { idx: 1 },
          createdAt: new Date("2026-04-19T04:02:03.000Z"),
          updatedAt: new Date("2026-04-19T04:02:03.000Z"),
          deletedAt: null,
        } as unknown as QuizCommentEntity,
      ],
      totalCount: 1,
      totalPages: 1,
      currentPage: 1,
      hasNext: false,
    };

    mockEntityDetail = {
      idx: 2,
      state: BoardState.PUBLIC,
      userIdx: 1,
      content: "테스트 댓글 추가/수정 1",
      board_idx: 1,
      parent: { idx: 1 },
      createdAt: new Date("2026-04-19T04:02:03.000Z"),
      updatedAt: new Date("2026-04-19T04:02:03.000Z"),
      deletedAt: null,

      update: (content: string, state: BoardState) => {},
    };

    mockUpsertCommentReq = {
      content: "테스트 댓글 추가/수정 1",
      state: BoardState.PUBLIC,
      parent: 1,
    } as unknown as UpsertCommentReqType;

    mockUserInfo = {
      userIdx: 1,
      role: UserRole.USER,
      nickName: "테스트 유저 1",
    } as unknown as UserInfoResponse;

    mockCommentRepo = {
      findCommentByPaging: async () => mockEntityPage,
      save: async () => mockEntityDetail,
      create: () => mockEntityDetail,
      findCommentRepliesByPaging: async () => mockEntityReplyPage,
      findOneByOrFail: async () => mockEntityDetail,
    } as unknown as QuizCommentRepository;

    mockUserClient = {
      getUserListInfo: async () => ({ users: [mockUserInfo] }),
    } as unknown as UserClient;

    quizCommentService = new QuizCommentService(
      mockCommentRepo,
      mockUserClient,
    );
  });

  afterEach(() => {
    mock.restoreAll();
  });

  it("최상위 댓글 목록 조회시 유저 닉네임 포함해서 Page 반환", async () => {
    const pagingReq: PagingReqType = { page: 1, size: 10 };

    const result = await quizCommentService.getCommentPage(
      { quizIdx: 1 },
      pagingReq,
    );

    assert.ok(result.items[0]);
    assert.strictEqual(result.items[0].userNickName, "테스트 유저 1");
  });
  it("댓글 추가 시 닉네임 포함해서 단일 댓글 반환", async () => {
    const result = await quizCommentService.addComment(
      { userIdx: 1, userRole: UserRole.USER, userNickName: "테스트 유저 1" },
      { quizIdx: 1 },
      mockUpsertCommentReq,
    );

    assert.ok(result.updatedAt);
    assert.ok(result.idx);
    assert.strictEqual(result.userNickName, "테스트 유저 1");
  });
  it("대댓글 조회 시 유저 닉네임 포함해서 Page 반환", async () => {
    const parentIdx = { commentIdx: 1 };
    const paginReq: PagingReqType = { page: 1, size: 10 };
    const result = await quizCommentService.getCommentRepliesPage(
      parentIdx,
      paginReq,
    );

    assert.ok(result.items[0]);
    assert.strictEqual(result.items[0].userNickName, "테스트 유저 1");
    assert.equal(result.items[0].parent, parentIdx.commentIdx);
  });
  it("댓글 수정 시 닉네임 포함해서 단일 댓글 반환", async () => {
    const result = await quizCommentService.updateComment(
      { userIdx: 1, userRole: UserRole.USER, userNickName: "테스트 유저 1" },
      { commentIdx: 2 },
      mockUpsertCommentReq,
    );

    assert.ok(result.updatedAt);
    assert.ok(result.idx);
    assert.strictEqual(result.userNickName, "테스트 유저 1");
  });
});

describe("BoardCommentService 단위 테스트, 실패 케이스", () => {
  let quizCommentService: QuizCommentService;
  let mockCommentRepo: QuizCommentRepository;
  let mockUserClient: UserClient;

  let noDataPage: any;
  let mockUserInfo: any;
  let mockUserData: any;
  let mockUpsertCommentReq: any;

  const mockComment = new QuizCommentEntity();
  Object.assign(mockComment, {
    idx: 2,
    userIdx: 1,
    content: "기존 내용",
    state: BoardState.PUBLIC,
  });

  const hackerUserData: UserData = {
    userIdx: 999,
    userRole: UserRole.USER,
    userNickName: "이상힌놈",
  };

  beforeEach(() => {
    noDataPage = {
      items: [],
      totalCount: 0,
      currentPage: 10,
      totalPages: 3,
      hasNext: false,
    };

    mockUserInfo = {
      userIdx: 1,
      role: UserRole.USER,
      nickName: "테스트 유저 1",
    } as unknown as UserInfoResponse;

    mockUserData = {
      userIdx: 1,
      userRole: UserRole.USER,
      userNickName: "테스트 유저 1",
    } as unknown as UserData;

    mockUpsertCommentReq = {
      content: "테스트 댓글 추가/수정 1",
      state: BoardState.PUBLIC,
      parent: 1,
    } as unknown as UpsertCommentReqType;

    mockCommentRepo = {
      findCommentByPaging: async () => noDataPage,
      findCommentRepliesByPaging: async () => noDataPage,
      findOneByOrFail: async () => {
        throw new Error("EntityNotFoundError");
      },
    } as unknown as QuizCommentRepository;

    mockUserClient = {
      getUserListInfo: async () => ({ users: [mockUserInfo] }),
    } as unknown as UserClient;

    quizCommentService = new QuizCommentService(
      mockCommentRepo,
      mockUserClient,
    );
  });

  afterEach(() => {
    mock.restoreAll();
  });

  it("최상위 댓글 목록 조회시 데이터가 없을 경우 빈 Page 반환", async () => {
    const pagingReq: PagingReqType = { page: 10, size: 10 };
    const result = await quizCommentService.getCommentPage(
      { quizIdx: 1 },
      pagingReq,
    );

    assert.strictEqual(result.items.length, 0);
  });

  it("대댓글 조회 시 데이터가 없을 경우 빈 Page 반환", async () => {
    const pagingReq: PagingReqType = { page: 10, size: 10 };
    const result = await quizCommentService.getCommentRepliesPage(
      { commentIdx: 1 },
      pagingReq,
    );

    assert.strictEqual(result.items.length, 0);
  });
  it("댓글 수정 시 수정 할 댓글이 없을 경우 에러 던지기", async () => {
    await assert.rejects(
      async () => {
        await quizCommentService.updateComment(
          mockUserData,
          { commentIdx: 2 },
          mockUpsertCommentReq,
        );
      },
      (err: Error) => {
        assert.strictEqual(err.message, "EntityNotFoundError");
        return true;
      },
    );
  });
  it("댓글 수정 시 본인이 아닐시 에러 던지기", async () => {
    mockCommentRepo.findOneByOrFail = async () => mockComment;

    await assert.rejects(
      async () => {
        await quizCommentService.updateComment(
          hackerUserData,
          { commentIdx: 2 },
          mockUpsertCommentReq,
        );
      },
      (err: Error) => {
        assert.strictEqual(err.name, "ForbiddenError");
        return true;
      },
    );
  });
  it("댓글 삭제 시 삭제 할 댓글이 없을 경우 에러 던지기", async () => {
    await assert.rejects(
      async () => {
        await quizCommentService.deleteComment(mockUserData, { commentIdx: 2 });
      },
      (err: Error) => {
        assert.strictEqual(err.message, "EntityNotFoundError");
        return true;
      },
    );
  });
  it("댓글 삭제 시 본인이 아닐시 에러 던지기", async () => {
    mockCommentRepo.findOneByOrFail = async () => mockComment;

    await assert.rejects(
      async () => {
        await quizCommentService.deleteComment(hackerUserData, {
          commentIdx: 2,
        });
      },
      (err: Error) => {
        assert.strictEqual(err.name, "ForbiddenError");
        return true;
      },
    );
  });
});
