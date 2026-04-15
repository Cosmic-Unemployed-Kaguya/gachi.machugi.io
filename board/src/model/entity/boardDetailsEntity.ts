import { Column, CreateDateColumn, DeleteDateColumn, Entity, JoinColumn, ManyToOne, PrimaryGeneratedColumn, Timestamp, TreeParent, UpdateDateColumn } from "typeorm";
import { BoardEntity } from './boardEntity';
import { DefaultEntity } from "./defaultEntity";
import { BoardState } from "../vo/boardState";

@Entity('board_detail')
export class BoardDetailEntity extends DefaultEntity{

    /**
     * idx , createdAt, updatedAt, deletedAt 포함
     *
     */


    @Column({
        name: "content",
        nullable : false,
    })
    content : string;

    @Column({
        name: "state",
        type: "enum",
        enum : BoardState,
        nullable : false,
        default : BoardState.PUBLIC
    })
    state: BoardState;

    @Column({
        name: "user_idx",
        nullable : false,
    })   
    userIdx: number;

    @ManyToOne((type) => BoardEntity)
    @JoinColumn({name: "board_idx"})
    board: BoardEntity;

    @TreeParent()
    @JoinColumn({name: "parent_idx"})
    parent: BoardDetailEntity;

}