import { Router } from "express";

import boardComment from "./board-comment/boardCommentRoutes";
import BoardCommentGrpcServer from "./board-comment/boardCommentServer";
import notice from "./board/noticeRoutes";
import NoticeGrpcServer from "./board/noticeServer";
import quiz from "./quiz-comment/quizCommentRoutes";
import QuizCommentGrpcServer from "./quiz-comment/quizCommentServer";

export const grpcServers: any[] = [
  NoticeGrpcServer,
  QuizCommentGrpcServer,
  BoardCommentGrpcServer,
];

export const router = Router();

// router.use ('/경로' , 하위 라우터  )
// 이곳에서 notice, quizComment 등 하위 라우터들을 총합해서 외부 loader로 보내줄것임

router.use("/notice", notice);
router.use("/notice", boardComment)
router.use("/quiz", quiz);
// 이하 라우터 추가~~
