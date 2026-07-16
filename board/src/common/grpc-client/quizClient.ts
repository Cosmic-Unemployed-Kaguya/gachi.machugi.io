import config from "../config";

import { GrpcClient, GrpcClientProperty } from "@decorator/grpcClient";

import {
  IsQuizRespons,
  QuizIdxRequest,
  QuizServiceClient,
} from "@generated/machugi/board/quiz";

@GrpcClient(QuizServiceClient, config.quizService)
export default class QuizGrpcClient {
  @GrpcClientProperty()
  public isQuiz: (req: QuizIdxRequest) => Promise<IsQuizRespons>;
}
