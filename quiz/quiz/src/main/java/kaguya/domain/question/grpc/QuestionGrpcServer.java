package kaguya.domain.question.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.domain.question.model.dto.request.QuestionRequest;
import kaguya.domain.question.service.QuestionService;
import kaguya.grpc.quiz.CreateQuestionRequest;
import kaguya.grpc.quiz.DeleteQuestionRequest;
import kaguya.grpc.quiz.GetQuestionListRequest;
import kaguya.grpc.quiz.GetQuestionListResponse;
import kaguya.grpc.quiz.GetQuestionRequest;
import kaguya.grpc.quiz.QuestionResponse;
import kaguya.grpc.quiz.QuestionServiceGrpc;
import kaguya.grpc.quiz.UpdateQuestionRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class QuestionGrpcServer
        extends QuestionServiceGrpc.QuestionServiceImplBase {

    private final QuestionService questionService;

    @Override
    public void createQuestion(
            CreateQuestionRequest request,
            StreamObserver<QuestionResponse> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인
        // TODO: 입력값 검증 및 상위 퀴즈 존재 여부 확인

        QuestionRequest reqData = new QuestionRequest(
                request.hasProblemText() ? request.getProblemText() : null,
                request.hasProblemUrl() ? request.getProblemUrl() : null,
                request.getType(),
                request.getSortOrder()
        );

        var resData = questionService.createQuestion(
                request.getQuizIdx(),
                reqData
        );

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void getQuestion(
            GetQuestionRequest request,
            StreamObserver<QuestionResponse> responseObserver
    ) {
        var resData = questionService.getQuestion(request.getQuestionIdx());

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void getQuestionList(
            GetQuestionListRequest request,
            StreamObserver<GetQuestionListResponse> responseObserver
    ) {
        var questionList = questionService
                .getQuestionList(request.getQuizIdx())
                .stream()
                .map(this::toProto)
                .toList();

        GetQuestionListResponse response =
                GetQuestionListResponse.newBuilder()
                        .addAllQuestionList(questionList)
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateQuestion(
            UpdateQuestionRequest request,
            StreamObserver<QuestionResponse> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인
        // TODO: 입력값 검증

        QuestionRequest reqData = new QuestionRequest(
                request.hasProblemText() ? request.getProblemText() : null,
                request.hasProblemUrl() ? request.getProblemUrl() : null,
                request.hasType() ? request.getType() : null,
                request.hasSortOrder() ? request.getSortOrder() : null
        );

        var resData = questionService.patchQuestion(
                request.getQuestionIdx(),
                reqData
        );

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteQuestion(
            DeleteQuestionRequest request,
            StreamObserver<Empty> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인

        questionService.deleteQuestion(request.getQuestionIdx());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private QuestionResponse toProto(
            kaguya.domain.question.model.dto.response.QuestionResponse data
    ) {
        return QuestionResponse.newBuilder()
                .setIdx(data.idx())
                .setQuizIdx(data.quizIdx())
                .setProblemText(textOrEmpty(data.problemText()))
                .setProblemUrl(textOrEmpty(data.problemUrl()))
                .setType(data.type())
                .setSortOrder(data.sortOrder())
                .setCreatedDate(data.createdDate().toString())
                .setUpdatedDate(data.updatedDate().toString())
                .build();
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }
}