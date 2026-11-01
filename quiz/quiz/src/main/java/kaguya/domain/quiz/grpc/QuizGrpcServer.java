package kaguya.domain.quiz.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import kaguya.domain.quiz.grpc.interceptor.GrpcContextKeys;
import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.request.QuizUpdateRequest;
import kaguya.domain.quiz.service.QuizService;
import kaguya.grpc.quiz.CreateQuizRequest;
import kaguya.grpc.quiz.DeleteQuizRequest;
import kaguya.grpc.quiz.GetQuizListResponse;
import kaguya.grpc.quiz.GetQuizRequest;
import kaguya.grpc.quiz.QuizResponse;
import kaguya.grpc.quiz.QuizServiceGrpc;
import kaguya.grpc.quiz.UpdateQuizRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class QuizGrpcServer extends QuizServiceGrpc.QuizServiceImplBase {

    private final QuizService quizService;

    @Override
    public void createQuiz(
            CreateQuizRequest request,
            StreamObserver<QuizResponse> responseObserver
    ) {
        Long creatorId = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();

        if (creatorId == null
                || !("USER".equals(role) || "ADMIN".equals(role))) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Login is required")
                            .asRuntimeException()
            );
            return;
        }

        QuizCreateRequest reqData = new QuizCreateRequest(
                request.getTitle(),
                request.getDescription(),
                request.hasThumbnail() ? request.getThumbnail() : null,
                request.hasCategory() ? request.getCategory() : null,
                creatorId
        );

        var resData = quizService.createQuiz(reqData);

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void getQuiz(
            GetQuizRequest request,
            StreamObserver<QuizResponse> responseObserver
    ) {
        var resData = quizService.getQuiz(request.getQuizIdx());

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void getQuizList(
            Empty request,
            StreamObserver<GetQuizListResponse> responseObserver
    ) {
        var quizList = quizService.getQuizList().stream()
                .map(this::toProto)
                .toList();

        GetQuizListResponse response = GetQuizListResponse.newBuilder()
                .addAllQuizList(quizList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateQuiz(
            UpdateQuizRequest request,
            StreamObserver<QuizResponse> responseObserver
    ) {
        // 다음 ISSUE 투두 : 로그인 및 작성자 권한 확인

        QuizUpdateRequest reqData = new QuizUpdateRequest(
                request.hasTitle() ? request.getTitle() : null,
                request.hasDescription() ? request.getDescription() : null,
                request.hasThumbnail() ? request.getThumbnail() : null,
                request.hasCategory() ? request.getCategory() : null
        );

        var resData = quizService.updateQuiz(request.getQuizIdx(), reqData);

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteQuiz(
            DeleteQuizRequest request,
            StreamObserver<Empty> responseObserver
    ) {
        // 다음 ISSUE 투두: 로그인 및 작성자 권한 확인

        quizService.deleteQuiz(request.getQuizIdx());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private QuizResponse toProto(
            kaguya.domain.quiz.model.dto.response.QuizResponse data
    ) {
        return QuizResponse.newBuilder()
                .setIdx(data.idx())
                .setTitle(data.title())
                .setDescription(textOrEmpty(data.description()))
                .setQuestionCount(data.questionCount())
                .setThumbnail(textOrEmpty(data.thumbnail()))
                .setCreatorId(data.creatorId())
                .setCreatedDate(data.createdDate().toString())
                .setUpdatedDate(data.updatedDate().toString())
                .setCategory(textOrEmpty(data.category()))
                .build();
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }
}