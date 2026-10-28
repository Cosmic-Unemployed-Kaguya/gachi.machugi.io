import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";

import config from "../config";

import { IsQuizRespons, QuizIdxRequest, QuizInternalServiceClient } from "@generated/machugi/quiz/quiz_internal";

@GrpcClient(QuizInternalServiceClient, config.quizService)
export default class QuizGrpcClient {
  @GrpcClientProperty()
  public isQuiz: (req: QuizIdxRequest) => Promise<IsQuizRespons>;
}
