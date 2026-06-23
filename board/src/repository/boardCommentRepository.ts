

import { TypeOrmRepository } from './typeOrmBaseRepository';
import { Repository } from '../decorator/repository';
import { BoardCommentEntity } from '../model/entity/boardComment';
import { Page, PagingReqType } from '../model/dto/paging';

@Repository(BoardCommentEntity)
export class BoardCommentRepository extends TypeOrmRepository<BoardCommentEntity>{

    /**
     * 최상위 댓글 조회
     * @param boardIdx 
     * @param pagingDTO 
     * @returns Page<BoardCommentEntity>
     */ 
    async findCommentByPaging( boardIdx : number,pagingDTO :PagingReqType) : Promise<Page<BoardCommentEntity>>{

        // 0. 해당 개시글에 대한 댓글 조회
        const qb = this.createQueryBuilder("comment")
            .where("comment.board = :boardIdx", {boardIdx})

        // 1. 최상위 댓글만 조회
        qb.andWhere("comment.parent IS NULL");
        

        // 2. 검색 범위 설정  < 댓글 검색이 필요한가? 싶긴한데 일단 넣어두기는 해봄
        if (pagingDTO.search){
            qb.andWhere("comment.content LIKE :search",{search: `%${pagingDTO.search}%` })
        }
        // 3. filter는 일단 스킵

        // 4. 그 외 페이징 처리
        return await this.paginate(qb, pagingDTO);
    }

    /**
     * 대댓글 조회
     * @param parentIdx 
     * @param pagingDTO 
     * @returns Page<BoardCommentEntity>
     */
    async findCommentRepliesByPaging( parentIdx : number,pagingDTO :PagingReqType) : Promise<Page<BoardCommentEntity>>{

        // 0. 해당 댓글에 대한 대댓글 조회
        const qb = this.createQueryBuilder("comment")
            .where("comment.parent = :parentIdx", {parentIdx});
        

        // 2. 검색 범위 설정  < 댓글 검색이 필요한가? 싶긴한데 일단 넣어두기는 해봄
        if (pagingDTO.search){
            qb.andWhere("comment.content LIKE :search",{search: `%${pagingDTO.search}%` })
        }
        // 3. filter는 일단 스킵

        // 4. 그 외 페이징 처리
        return await this.paginate(qb, pagingDTO);
    }

}