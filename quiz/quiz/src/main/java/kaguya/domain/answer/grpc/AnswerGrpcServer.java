package kaguya.domain.answer.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.domain.answer.model.dto.request.AnswerRequest;
import kaguya.domain.answer.service.AnswerService;
import kaguya.grpc.quiz.AnswerResponse;
import kaguya.grpc.quiz.AnswerServiceGrpc;
import kaguya.grpc.quiz.CreateAnswerRequest;
import kaguya.grpc.quiz.DeleteAnswerRequest;
import kaguya.grpc.quiz.GetAnswerListRequest;
import kaguya.grpc.quiz.GetAnswerListResponse;
import kaguya.grpc.quiz.UpdateAnswerRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AnswerGrpcServer
        extends AnswerServiceGrpc.AnswerServiceImplBase {

    private final AnswerService answerService;

    @Override
    public void createAnswer(
            CreateAnswerRequest request,
            StreamObserver<AnswerResponse> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인
        // TODO: 입력값 검증 및 상위 문제 존재 여부 확인

        AnswerRequest reqData = new AnswerRequest(request.getAnswer());

        var resData = answerService.createAnswer(
                request.getQuestionIdx(),
                reqData
        );

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void getAnswerList(
            GetAnswerListRequest request,
            StreamObserver<GetAnswerListResponse> responseObserver
    ) {
        // TODO: 정답 편집용 조회에 대한 작성자 권한 확인

        var answerList = answerService
                .getAnswerList(request.getQuestionIdx())
                .stream()
                .map(this::toProto)
                .toList();

        GetAnswerListResponse response =
                GetAnswerListResponse.newBuilder()
                        .addAllAnswerList(answerList)
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAnswer(
            UpdateAnswerRequest request,
            StreamObserver<AnswerResponse> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인
        // TODO: 입력값 검증

        AnswerRequest reqData = new AnswerRequest(
                request.hasAnswer() ? request.getAnswer() : null
        );

        var resData = answerService.patchAnswer(
                request.getAnswerIdx(),
                reqData
        );

        responseObserver.onNext(toProto(resData));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAnswer(
            DeleteAnswerRequest request,
            StreamObserver<Empty> responseObserver
    ) {
        // TODO: 로그인 및 퀴즈 작성자 권한 확인

        answerService.deleteAnswer(request.getAnswerIdx());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private AnswerResponse toProto(
            kaguya.domain.answer.model.dto.response.AnswerResponse data
    ) {
        return AnswerResponse.newBuilder()
                .setIdx(data.idx())
                .setQuestionIdx(data.questionIdx())
                .setAnswer(data.answer())
                .setCreatedDate(data.createdDate().toString())
                .setUpdatedDate(data.updatedDate().toString())
                .build();
    }
}