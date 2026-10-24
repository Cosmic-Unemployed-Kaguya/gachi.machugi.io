import BoardGrpcServer from "@domain/board-comment/boardCommentServer";
import NoticeGrpcServer from "@domain/board/noticeServer";
import QuizCommentGrpcServer from "@domain/quiz-comment/quizCommentServer";

export const grpcServers: any[] = [NoticeGrpcServer, QuizCommentGrpcServer, BoardGrpcServer];
