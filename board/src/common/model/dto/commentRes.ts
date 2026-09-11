
import { BoardState } from "../enum/boardState";

// 댓글 하나만 반환 (추가 ,수정 시 )

// Res
export interface CommentRes {
  idx: number;
  parent: number | null;
  userNickName: string;
  content: string;
  state: BoardState;
  updatedAt: Date;
}
