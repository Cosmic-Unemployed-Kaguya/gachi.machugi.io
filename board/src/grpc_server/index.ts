import NoticeGrpcServer from "./noticeServer"
import QuizCommentGrpcServer from "./quizCommentServer"

const grpcServers: any[] = [NoticeGrpcServer, QuizCommentGrpcServer]

export default grpcServers
