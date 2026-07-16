import {
  CreateDateColumn,
  DeleteDateColumn,
  PrimaryGeneratedColumn,
  Timestamp,
  UpdateDateColumn,
} from "typeorm";

export abstract class DefaultEntity {
  @PrimaryGeneratedColumn({
    name: "idx",
  })
  idx: number;

  @CreateDateColumn({
    name: "created_at",
    nullable: false,
  })
  createdAt: Date;

  @UpdateDateColumn({
    name: "updated_at",
    nullable: false,
  })
  updatedAt: Date;

  @DeleteDateColumn({
    name: "deleted_at",
  })
  deletedAt: Date;
}
