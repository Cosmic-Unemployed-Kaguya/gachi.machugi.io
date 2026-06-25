import config from "../config";
import { GrpcClient, GrpcClientProperty } from "../decorator/grpcClient";
import { IsQuizRes, QuizClient, QuizIdxReq } from "../generated/machugi/board/quiz";

@GrpcClient(QuizClient, config.quizService)
export default class QuizGrpcClient{

    @GrpcClientProperty()
    public isQuiz: (req: QuizIdxReq) => Promise<IsQuizRes>; 
}