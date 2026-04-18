

import { TypeOrmRepository } from './TypeOrmBaseRepository';
import { Repository } from '../decorator/repository';
import { BoardEntity } from '../model/entity/boardEntity';
import { BoardType } from '../model/enum/boardType';
import { Page, PagingReqType } from '../model/dto/paging';

@Repository(BoardEntity)
export class BoardRepository extends TypeOrmRepository<BoardEntity>{

    async findNoticeByPaging( pagingDTO :PagingReqType) : Promise<Page<BoardEntity>>{
        // 1. type이 notice인거
        const qb = this.createQueryBuilder("board")
            .where("board.type = :type", {type : BoardType.NOTICE})

        // 2. 검색 범위 설정
        if (pagingDTO.search){
            qb.andWhere("board.title LIKE :search",{search: '%${pagingDTO.search}%' })
        }
        // 3. filter는 일단 스킵
        // 4. 그 외 페이징 처리
        return await this.paginate(qb, pagingDTO);
    }
    
}