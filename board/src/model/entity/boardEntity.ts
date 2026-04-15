import { Column, CreateDateColumn, DeleteDateColumn, Entity, PrimaryGeneratedColumn, Timestamp, UpdateDateColumn } from "typeorm";
import { BoardType } from "../vo/boardType";
import { BoardState } from "../vo/boardState";
import { DefaultEntity } from "./defaultEntity";

@Entity('board')
export class BoardEntity extends DefaultEntity{

    

    /**
     * idx , createdAt, updatedAt, deletedAt 포함
     *
     */

    @Column({
        name: "title",
        nullable : false
    })
    title : string;

    @Column({
        name: "type",
        type: "enum",
        enum : BoardType,
        nullable : false,
        default: BoardType.COMMUNITY,
    })
    type: string;

    @Column({
        name: "state",
        type: "enum",
        enum: BoardState,
        nullable : false,
        default: BoardState.PUBLIC,
    })   
    state: string;

    @Column({
        name: "is_pinned",
        nullable : false
    })
    isPinned: boolean;

    @Column({
        name: "view_count",
        nullable : false
    })
    viewCount: number;


}