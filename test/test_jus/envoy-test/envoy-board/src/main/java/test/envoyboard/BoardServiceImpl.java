package test.envoyboard;

import com.example.grpc.board.BoardServiceGrpc;
import com.example.grpc.board.GetPostRequest;
import com.example.grpc.board.GetPostResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Postman으로
 * GET http://localhost:8080/board/123
 * header 설정: key(Host):value(board.example.com)
 *
 * Json 형식으로 return 잘 오면 성공
 */

@GrpcService
public class BoardServiceImpl extends BoardServiceGrpc.BoardServiceImplBase {
    @Override
    public void getPost(GetPostRequest request, StreamObserver<GetPostResponse> responseObserver) {
        GetPostResponse response = GetPostResponse.newBuilder()
                .setPostId(request.getPostId())
                .setTitle("공지사항")
                .setContent("envoy가 정상적으로 작동됨")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
