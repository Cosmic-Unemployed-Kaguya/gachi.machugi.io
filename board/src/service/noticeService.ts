import { Inject, Service } from "typedi";
import { BoardRepository } from "../repository/boardRepository";
import { Page, PagingReq, PagingReqType } from './../model/dto/paging';
import { BoardEntity } from "../model/entity/boardEntity";
import { NoticeListRes } from "../model/dto/noticeListDTO";
import { createDTO, toPageDTO } from "../utils/mapper/pageMapper";
import { BoardCommentEntity } from "../model/entity/boardComment";
import { BoardCommentRepository } from '../repository/boardCommentRepository';
import { UpsertNoticeType } from "../model/dto/upsertNoticeReq";
import { NoticeDetailRes } from "../model/dto/getNoticeDetailDTO";
import { BoardIdxParamReqType } from './../model/dto/IdxParamReq';
import { BoardType } from "../model/enum/boardType";
import { toNoticeDetail, toNoticeListRes } from "../utils/mapper/noticeMapper";



@Service()
export default class NoticeService{
    constructor(
        @Inject() private boardRepository : BoardRepository,
        @Inject() private boardCommentRepository : BoardCommentRepository,
    ){}

    /**
     * 모든 공지 조회
     * @param pagingDTO 
     * @returns 
     */
    public async getNoticeList(pagingReq : PagingReqType) : Promise<Page<NoticeListRes>>{
        // DB에서 조회한 Entity Page
        const pagingEntity :Page<BoardEntity> = await this.boardRepository.findNoticeByPaging(pagingReq)
        
        // // Page<Entity>  -> Page<DTO>
        // const pagingRes : Page<NoticeListRes> = createDTO(pagingEntity, ['idx','title','state','viewCount','isPinned','updatedAt'])
        
        // return pagingRes;
        
        const pagingDtoRes : Page<NoticeListRes> = toPageDTO(pagingEntity, toNoticeListRes)

        return pagingDtoRes;
    }


    public async addNotice(userIdx : number , upsertNoticeReq : UpsertNoticeType) : Promise<NoticeDetailRes>{

        // 1. Board
        // 1.1 Entity Set  > entity 객체를 만들 때, 비어있는 값(idx, createdAt 등 )이 존재하게 만드려면 
        //                   repository의 create 메서드 사용
        const board :BoardEntity = this.boardRepository.create({
            title : upsertNoticeReq.title,
            content : upsertNoticeReq.content,
            isPinned : upsertNoticeReq.isPinned,
            state : upsertNoticeReq.state,
            userIdx : userIdx,
            viewCount : 0,
            type : BoardType.NOTICE,
        });


        // 1.2 save > DB에 저장
        const savedBoard : BoardEntity =  await this.boardRepository.save(board);

        // 2. 추가이므로 댓글 x

        // 3. mapper dto 만들어서 반환
        return toNoticeDetail(savedBoard);
        
    }

    public async getNoticeDetail(noticeIdxParam : BoardIdxParamReqType) : Promise<NoticeDetailRes>{

        const board :BoardEntity = await this.boardRepository.findOneByOrFail({idx: noticeIdxParam.boardIdx });

        return toNoticeDetail(board);
    }


    public async updateNotice( noticeIdxParam : BoardIdxParamReqType, upsertNoticeReq : UpsertNoticeType) : Promise<NoticeDetailRes>{

        // 1. board 조회 , 없을시 에러
        const board :BoardEntity = await this.boardRepository.findOneByOrFail({idx: noticeIdxParam.boardIdx });

        // 2. board entity 수정
        board.update(upsertNoticeReq.title, upsertNoticeReq.state, upsertNoticeReq.isPinned , upsertNoticeReq.content);
    
        // 3. 수정 된 entity 저장(업데이트)
        const savedBoard = await this.boardRepository.save(board);


        // 5. 반환
        return toNoticeDetail(savedBoard);


    }

    public async deleteNotice(noticeIdxParam : BoardIdxParamReqType) :  Promise<Page<NoticeListRes>>{

        // 1. board 조회, 없을 시 에러
        const board :BoardEntity = await this.boardRepository.findOneByOrFail({idx: noticeIdxParam.boardIdx });

        // 1.1 comment 조회
        const boardComment : BoardCommentEntity[] =  await this.boardCommentRepository.findBy({board: board});

        // 2. board delete 시도
        await this.boardRepository.softRemove(board);

        // 2.1 comment delete 시도
        await this.boardCommentRepository.softRemove(boardComment);

        // 3. 반환 용 데이터 생성
        // DB에서 조회한 Entity Page
        const pagingData :Page<BoardEntity> = await this.boardRepository.findNoticeByPaging({page:1, size:20})
        
        // Page<Entity>  -> Page<DTO>
        const pagingDtoRes : Page<NoticeListRes> = toPageDTO(pagingData, toNoticeDetail)

        return pagingDtoRes;

    }
}