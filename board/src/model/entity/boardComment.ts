import { Column, CreateDateColumn, DeleteDateColumn, Entity, JoinColumn, ManyToOne, PrimaryGeneratedColumn, Timestamp, Tree, TreeParent, UpdateDateColumn } from "typeorm";
import { BoardEntity } from './boardEntity';
import { DefaultEntity } from "./defaultEntity";
import { BoardState } from "../enum/boardState";
import { nullable } from './../../../node_modules/zod/v4/classic/schemas';

@Entity('board_comment')
@Tree("closure-table")
export class BoardCommentEntity extends DefaultEntity{

    /**
     * idx , createdAt, updatedAt, deletedAt 포함
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
    @JoinColumn({name: "parent_idx",})
    parent: BoardCommentEntity;


    

    public update(
        content: string,
        state : BoardState,
    ){
        this.content = content;
        this.state = state ; 


    }
}