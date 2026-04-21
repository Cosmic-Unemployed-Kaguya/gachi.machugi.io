package test.envoyquiz;

import com.example.grpc.quiz.GetQuizRequest;
import com.example.grpc.quiz.GetQuizResponse;
import com.example.grpc.quiz.QuizServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Postman으로
 * GET http://localhost:8080/quiz/123
 * header 설정: key(Host):value(user.example.com)
 *
 * Json 형식으로 return 잘 오면 성공
 */

@GrpcService
public class QuizServiceImpl extends QuizServiceGrpc.QuizServiceImplBase {
    @Override
    public void getQuiz(GetQuizRequest request, StreamObserver<GetQuizResponse> responseObserver) {
        GetQuizResponse response = GetQuizResponse.newBuilder()
                .setQuizId(request.getQuizId())
                .setTitle("Envoy 테스트 퀴즈")
                .setDifficulty("EASY")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}