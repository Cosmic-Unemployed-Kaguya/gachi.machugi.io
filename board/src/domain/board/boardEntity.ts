import { Column, Entity } from "typeorm";

import { DefaultEntity } from "@common/utils/defaultEntity";

import { BoardState } from "@enum/boardState";
import { BoardType } from "@enum/boardType";

@Entity("board")
export class BoardEntity extends DefaultEntity {
  /**
   * idx , createdAt, updatedAt, deletedAt 포함
   */

  @Column({
    name: "title",
    nullable: false,
  })
  title: string;

  @Column({
    name: "content",
    nullable: false,
  })
  content: string;

  @Column({
    name: "user_idx",
    nullable: false,
  })
  userIdx: number;

  @Column({
    name: "type",
    type: "enum",
    enum: BoardType,
    nullable: false,
    default: BoardType.COMMUNITY,
  })
  type: BoardType;

  @Column({
    name: "state",
    type: "enum",
    enum: BoardState,
    nullable: false,
    default: BoardState.PUBLIC,
  })
  state: BoardState;

  @Column({
    name: "is_pinned",
    nullable: false,
  })
  isPinned: boolean;

  @Column({
    name: "view_count",
    nullable: false,
  })
  viewCount: number;

  public update(
    title: string,
    state: BoardState,
    isPinned: boolean,
    content: string,
  ) {
    this.title = title;
    this.state = state;
    this.isPinned = isPinned;
    this.content = content;
  }
}
