import { CreateDateColumn, DeleteDateColumn, PrimaryGeneratedColumn, Timestamp, UpdateDateColumn } from "typeorm";

export abstract class DefaultEntity{

    @PrimaryGeneratedColumn({
        name: "idx",
    })
    idx : number;

    @CreateDateColumn({
        name: "created_at",
        nullable : false
    })
    createdAt: Timestamp;

    @UpdateDateColumn({
        name: "updated_at",
        nullable : false
    })
    updatedAt: Timestamp;

    @DeleteDateColumn({
        name: "deleted_at",
    })
    deletedAt: Timestamp;
}