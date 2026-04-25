import assert from 'node:assert';
import {afterEach, before, beforeEach, describe, it, mock, test} from 'node:test';
import { getNoticeList } from '../src/controller/noticeController';
import { Page, PagingReqType } from '../src/model/dto/paging';
import { BoardState } from '../src/model/enum/boardState';
import { BoardType } from '../src/model/enum/boardType';
import { BoardEntity } from '../src/model/entity/boardEntity';
import Container from 'typedi';
import NoticeService from '../src/service/noticeService';
import { BoardRepository } from '../src/repository/boardRepository';
import { BoardCommentRepository } from '../src/repository/boardCommentRepository';
import { UpsertCommentReq } from '../src/model/dto/upsertCommentReq';
import { UpsertNoticeType } from '../src/model/dto/upsertNoticeReq';

// suite = describe  : 여러 test를 묶는 뭐시기
// test = it   : 테스트
const getTest = () :string => {return "test"}

describe('테스트의 테스트! 테스트를 위한 테스트!',() => {
    it("테스트의 영어 스펠링을 반환", () =>{
        const result = getTest()
        assert.equal(result, "test")
    });
});

describe('NoticeService 단위 테스트, 성공 케이스', () => {

    let noticeService: NoticeService;
    let mockBoardRepo: BoardRepository;
    let mockCommentRepo: BoardCommentRepository;
    let mockEntityPage: any; 
    let mockEntityDetail : any;

    let upsertNoticeReq :UpsertNoticeType; 

    beforeEach(() => {
         // db가 반환 할 가짜데이터
        mockEntityPage = {
            items: [
                {
                    idx: 1,
                    title: '테스트 공지사항입니다',
                    state: BoardState.PUBLIC,
                    viewCount: 150,
                    isPinned: true,
                    userIdx : 1,
                    type: BoardType.NOTICE,
                    createdAt : new Date('2026-04-19T04:02:03.000Z'),
                    updatedAt: new Date('2026-04-19T04:02:03.000Z'),
                    deletedAt : null,

                } as unknown as BoardEntity // 타입 단언으로 필요한 필드만 대충 채워줍니다
            ],
            totalCount : 1,
            totalPages: 1,
            currentPage : 1,
            hasNext : false,
        };

        mockEntityDetail = {
            idx: 1,
            title: '테스트 공지사항입니다',
            state: BoardState.PUBLIC,
            viewCount: 150,
            isPinned: true,
            userIdx : 1,
            type: BoardType.NOTICE,
            createdAt : new Date('2026-04-19T04:02:03.000Z'),
            updatedAt: new Date('2026-04-19T04:02:03.000Z'),
            deletedAt : null,
        } as unknown as BoardEntity 

        mockBoardRepo = {
            findNoticeByPaging : async () => mockEntityPage,
            save : async () => mockEntityDetail,
            findOneByOrFail : async () =>  mockEntityDetail,
            update : async () => mockEntityDetail,
            softRemove : async () => {},
            create: () => mockEntityDetail,
        } as unknown as BoardRepository;        

        mockCommentRepo = {
            softRemove : async () => {},
        } as unknown as BoardCommentRepository;     

        noticeService = new NoticeService(mockBoardRepo, mockCommentRepo);

        upsertNoticeReq  = {
            title: '테스트 공지사항입니다',
            state: BoardState.PUBLIC,
            isPinned: true,
            content: "안녕하세요. 관리자입니다. 2026년 4월 30일 새벽 2"
        }

        

        
    });
    
    // 각 테스트가 끝날 때 마다 mock을 초기화
    afterEach(() => {
        mock.restoreAll();
    });

    it('모든 공지 조회 시 Page<Entity> 객체를 Page<DTO>로 변환해서 반환 해야함', async () => {
        
        // 가짜 요청 생성
        const pagingReq : PagingReqType = { page: 1, size: 10 }; 

       
        // findNoticeByPaging 함수가 실제로 db와 연결되는것이 아닌, 위 가짜 데이터를 뱉도록 함
        
        // 테스트 함수 실행
        const result = await noticeService.getNoticeList(pagingReq);

        assert.strictEqual(result.items.length, 1);
        assert.strictEqual(result.items[0].title, '테스트 공지사항입니다');

    });

    it('공지 추가 시 Entity를 DTO로 변환 후 반환 ', async () =>{

        const result = await noticeService.addNotice(1,upsertNoticeReq);

        assert.ok(result.updatedAt);
        assert.ok(result.idx);
        assert.strictEqual(result.title, '테스트 공지사항입니다');
    })

    it('공지 조회 시  DTO로 잘 변환 후 반환', async () => {

        const result = await noticeService.getNoticeDetail({ boardIdx: 1 });
        assert.strictEqual(result.title, mockEntityDetail.title); // 변환 확인
    });


});





// service에서 에러를 처리하지 않고, 밖으로 뱉어야함
describe('NoticeService 단위 테스트, 에러 케이스', () => {
    
    let noticeService: NoticeService;
    let mockBoardRepo: BoardRepository;
    let mockCommentRepo: BoardCommentRepository;

    let upsertNoticeReq :UpsertNoticeType; 
    let noDataPage : Page<BoardEntity>
    beforeEach(() => {

        mockBoardRepo = {
            findOneByOrFail : async () => { throw new Error('EntityNotFoundError'); },
            findNoticeByPaging : async () => {return noDataPage},

        } as unknown as BoardRepository;        

        mockCommentRepo = {
            softRemove : async () => {},
        } as unknown as BoardCommentRepository;     

        noticeService = new NoticeService(mockBoardRepo, mockCommentRepo);

        upsertNoticeReq  = {
            title: '테스트 공지사항입니다',
            state: BoardState.PUBLIC,
            isPinned: true,
            content: "안녕하세요. 관리자입니다. 2026년 4월 30일 새벽 2"
        }
        noDataPage = {
            items : [] ,
            totalCount : 0,
            currentPage : 10,
            totalPages: 3,
            hasNext : false,
        }
    });
    
    // 각 테스트가 끝날 때 마다 mock을 초기화
    afterEach(() => {
        mock.restoreAll();
    });
    it('공지 목록 조회 시 DB에 데이터가 없으면 빈 페이지 반환', async () => {
        const pagingReq : PagingReqType = { page: 10, size: 10 }; 
        const result = await noticeService.getNoticeList(pagingReq); 

        assert.strictEqual(result.items.length, 0 );


    });
    it('공지 상세 조회 시 DB에 데이터가 없으면 에러를 뱉어야함', async () => {
        await assert.rejects(
            async () => { await noticeService.getNoticeDetail({ boardIdx: 999 }); },
            (err: Error) => {
                assert.strictEqual(err.message, 'EntityNotFoundError');
                return true;
            }
            
        );
    });

    it('공지 수정 중 조회 시 DB에 데이터가 없으면 에러를 뱉어야함', async () => {
        await assert.rejects(
            async () => { await noticeService.updateNotice({boardIdx : 1}, upsertNoticeReq); },
            (err: Error) => {
                assert.strictEqual(err.message, 'EntityNotFoundError');
                return true;
            }
            
        );
    });
});