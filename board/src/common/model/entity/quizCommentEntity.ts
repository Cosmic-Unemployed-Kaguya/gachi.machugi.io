import { Column, Entity, JoinColumn, Tree, TreeParent } from "typeorm";

import { DefaultEntity } from "@common/utils/defaultEntity";

import { BoardState } from "@enum/boardState";

@Entity("quiz_comment")
@Tree("closure-table")
export class QuizCommentEntity extends DefaultEntity {
  /**
   * idx , createdAt, updatedAt, deletedAt 포함
   */

  @Column({
    name: "content",
    nullable: false,
  })
  content: string;

  @Column({
    name: "state",
    type: "enum",
    enum: BoardState,
    nullable: false,
    default: BoardState.PUBLIC,
  })
  state: BoardState;

  @Column({
    name: "user_idx",
    nullable: false,
  })
  userIdx: number;

  @Column({
    name: "quiz_idx",
    nullable: false,
  })
  quizIdx: number;

  @TreeParent()
  @JoinColumn({ name: "parent_idx" })
  parent: QuizCommentEntity;

  public update(content: string, state: BoardState) {
    this.content = content;
    this.state = state;
  }
}
