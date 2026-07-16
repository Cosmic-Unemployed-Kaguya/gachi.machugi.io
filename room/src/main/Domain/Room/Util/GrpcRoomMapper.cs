using System.Linq;
using Room.Model.Dto.Request;
using Room.Model.Dto.Response;
using GrpcRoom = Room.Service.Proto;

namespace Room.Util;

public static class RoomGrpcMapper
{
    //gRPC Request -> 기존 DTO 변환
    // 방 생성 요청 변환
    //optional 하면 Has어쩌구저쩌구가 생기는데 그걸로 있는지 확인하고 삼항으로 때려박으면 됨.
    public static CreateRoomRequest ToNativeRequest(this GrpcRoom.GrpcCreateRoomRequest request)
    {
        return new CreateRoomRequest(
            request.Name,
            request.MaxOccupancy,
            request.TimeLimit,
            request.IsPublic,
            request.HasPassword ? request.Password : null,
            request.QuizIdx,
            request.HostIdx
        );
    }

    //방 정보 수정 요청 변환
    public static UpdateRoomRequest ToNativeRequest(this GrpcRoom.GrpcUpdateRoomRequest request)
    {
        return new UpdateRoomRequest(
            request.Name,
            request.HasMaxOccupancy ? request.MaxOccupancy : 0,
            request.HasTimeLimit ? request.TimeLimit : 0,
            request.HasIsPublic ? request.IsPublic : true,
            request.HasPassword ? request.Password : null,
            request.HasQuizIdx ? request.QuizIdx : 0,
            request.HasHostId ? request.HostId : 0
        );
    }

    //플레이어 추가/제거 요청 변환
    public static UpdateSetRequest ToNativeRequest(this GrpcRoom.GrpcUpdateSetRequest request)
    {
        return new UpdateSetRequest(
            request.PlayerIdx
        );
    }
    //기존 Response -> gRPC Response 변환

    //RoomInfoResponse -> GrpcRoomInfoResponse
    public static GrpcRoom.GrpcRoomInfoResponse ToGrpcResponse(this RoomInfoResponse response)
    {
        return new GrpcRoom.GrpcRoomInfoResponse
        {
            Idx = response.Idx,
            HostIdx = response.hostIdx,
            Name = response.name,
            MaxOccupancy = response.maxOccupancy,
            TimeLimit = response.timeLimit,
            IsPublic = response.isPublic,
            QuizIdx = response.quizIdx
        };
    }

    //RoomSetInfoResponse -> GrpcRoomSetInfoResponse
    //이건 몰라서 gpt짱의 도움을 받음
    public static GrpcRoom.GrpcRoomSetInfoResponse ToGrpcResponse(this RoomSetInfoResponse response)
    {
        var grpcRes = new GrpcRoom.GrpcRoomSetInfoResponse();
        if (response.PlayerSet != null)
        {
            // repeated 필드는 읽기 전용이므로 AddRange로 기존 HashSet 데이터를 밀어 넣습니다.
            grpcRes.PlayerSet.AddRange(response.PlayerSet);
        }
        return grpcRes;
    }

    //RoomResponse -> GrpcRoomResponse
    public static GrpcRoom.GrpcRoomResponse ToGrpcResponse(this RoomResponse response)
    {
        return new GrpcRoom.GrpcRoomResponse
        {
            // 기존 객체 내부의 구성품들을 위에서 만든 매퍼로 각각 변환해 결합합니다.
            RoomInfo = response.roomInfo.ToGrpcResponse(),
            RoomSetInfo = response.setInfo.ToGrpcResponse()
        };
    }
}