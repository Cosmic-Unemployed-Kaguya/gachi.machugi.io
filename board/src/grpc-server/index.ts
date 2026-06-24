import NoticeGrpcServer from "./noticeServer"
import QuizCommentGrpcServer from "./quizCommentServer"
import BoardCommentGrpcServer from "./boardCommentServer"

const grpcServers: any[] = [NoticeGrpcServer, QuizCommentGrpcServer, BoardCommentGrpcServer]

export default grpcServers
