import { Router } from "express";

import {
  addNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticeList,
  updateNotice,
} from "./noticeController";

import { getUserAndRoleCheck } from "@common/middlewares/userCheck";
import { validate } from "@common/middlewares/validate";
import { UserRole } from "@common/model/enum/userRole";

import { BoardIdxParamReq } from "@dto/idxParamReq";
import { UpsertNoticeReq } from "@dto/noticeUpsertReq";
import { PagingReq } from "@dto/paging";

const route = Router();

/**
 * 모든 공지 조회, 추가
 */
route
  .route("/")
  .get(validate({ query: PagingReq }), getNoticeList)
  .post(
    validate({ body: UpsertNoticeReq }),
    getUserAndRoleCheck([UserRole.ADMIN]),
    addNotice,
  );

/**
 * 공지 상세 조회, 수정, 삭제
 */
route
  .route("/:boardIdx")
  // 상세 조회의 경우 로그인 확인 x
  .get(validate({ params: BoardIdxParamReq }), getNoticeDetail)

  .put(
    validate({ body: UpsertNoticeReq, params: BoardIdxParamReq }),
    getUserAndRoleCheck([UserRole.ADMIN]),
    updateNotice,
  )

  .delete(
    validate({ params: BoardIdxParamReq }),
    getUserAndRoleCheck([UserRole.ADMIN]),
    deleteNotice,
  );

export default route;
