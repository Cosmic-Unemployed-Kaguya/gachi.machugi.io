import { Column, Entity, JoinColumn, ManyToOne, Tree, TreeParent } from "typeorm";

import { DefaultEntity } from "@common/utils/defaultEntity";

import { BoardEntity } from "@common/model/entity/boardEntity";

import { BoardState } from "@enum/boardState";

@Entity("board_comment")
@Tree("closure-table")
export class BoardCommentEntity extends DefaultEntity {
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

  @ManyToOne((type) => BoardEntity)
  @JoinColumn({ name: "board_idx" })
  board: BoardEntity;

  @TreeParent()
  @JoinColumn({ name: "parent_idx" })
  parent: BoardCommentEntity;

  public update(content: string, state: BoardState) {
    this.content = content;
    this.state = state;
  }
}
