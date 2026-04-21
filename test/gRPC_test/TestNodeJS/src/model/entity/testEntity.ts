import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn, Timestamp } from "typeorm";

@Entity('type_orm_test')
export class TestEntity {

    // @PrimaryColumn()
    @PrimaryGeneratedColumn()
    id : number;

    @Column()
    name : string;

    @Column()
    description: string;

    @CreateDateColumn()
    createdAt: Timestamp;

}