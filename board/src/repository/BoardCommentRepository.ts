

import { TypeOrmRepository } from './TypeOrmBaseRepository';
import { Repository } from '../decorator/repository';
import { BoardCommentEntity } from '../model/entity/boardComment';

@Repository(BoardCommentEntity)
export class BoardCommentRepository extends TypeOrmRepository<BoardCommentEntity>{

    
}