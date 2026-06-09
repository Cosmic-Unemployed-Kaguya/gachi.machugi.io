import { BoardState as GrpcState} from "../../generated/common";
import { BoardState as AppState} from "../../model/enum/boardState";

export const toGrpcState : Record<AppState, GrpcState> = {
    [AppState.BLOCKED] : GrpcState.BLOCKED,
    [AppState.PRIVATE] : GrpcState.PRIVATE,
    [AppState.PUBLIC] : GrpcState.PUBLIC,
}

export function toAppState(grpcState:GrpcState) : AppState {
    switch(grpcState) {
        case GrpcState.BLOCKED:
            return AppState.BLOCKED;
        case GrpcState.PRIVATE:
            return AppState.PRIVATE;
        case GrpcState.PUBLIC:
            return AppState.PUBLIC;
        case GrpcState.UNRECOGNIZED:
        case GrpcState.UNSPECIFIED:
        default:
            return AppState.PUBLIC;
    }
}