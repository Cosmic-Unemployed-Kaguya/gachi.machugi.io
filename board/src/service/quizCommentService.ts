import { Inject, Service } from "typedi";
import { Page, PagingReqType } from "../model/dto/paging";
import {  CommentIdxParamReqType, QuizIdxParamReqType } from "../model/dto/idxParamReq";
import { CommentListRes } from "../model/dto/commentListRes";
import UserClient from "../grpc-client/userClient";
import { UserInfoListResponse, UserInfoListRequset } from "../generated/user";
import { UpsertCommentReqType } from "../model/dto/commentUpsertReq";
import { toPageDTO } from "../utils/mapper/pageMapper";
import { toCommentListRes, toCommentRes } from "../utils/mapper/commentMapper";
import { ForbiddenError } from "../utils/error";
import { UserData } from "../middlewares/appRequest";
import { CommentRes } from "../model/dto/commentRes";
import { CommentDeleteRes } from "../model/dto/commentDeleteRes";
import { QuizCommentRepository } from "../repository/quizCommentRepository";
import { QuizCommentEntity } from "../model/entity/quizComment";


@Service()
export default class QuizCommentService{
    constructor(
        @Inject() private quizCommentRepository : QuizCommentRepository,
        @Inject() private userClient : UserClient,
    ){}


    /**
     * 최상위 댓글 리스트 조회 (페이징)
     * @param quizIdx 
     * @param pagingReq 
     * @returns Page<CommentListRes>
     */
    public async getCommentPage(quizIdx : QuizIdxParamReqType ,pagingReq : PagingReqType) :  Promise<Page<CommentListRes>>{

        // 1. 댓글 데이터 조회
        const pagingEntity : Page<QuizCommentEntity> = await this.quizCommentRepository.findCommentByPaging(quizIdx.quizIdx, pagingReq);

        // 2. 댓글 작성자들 nickname 조회 
        const commenters : number []  = pagingEntity.items.map((entity) => entity.userIdx);

        const usersRes : UserInfoListResponse = await this.userClient.getUserListInfo({userIdxs : commenters});


        // 3. idx와 닉네임을 매핑하여 dto에 추가
        // 3.1 <idx: nickname> 형태로 매핑
        const userNicknameMap: Record<number, string> = usersRes.users.reduce<Record<number, string>>((acc, user) => {
            acc[user.userIdx] = user.nickName;
            return acc;
        }, {} );
        
        // 4. 반환 데이터 생성
        const pageDTO : Page<CommentListRes> = toPageDTO(pagingEntity, (entity) => toCommentListRes(entity, userNicknameMap));
        return pageDTO;
    }

    /**
     * 댓글 추가
     * @param userIdx 
     * @param quizIdx 
     * @param upsertCommnetReq 
     * @returns 댓글 리스트 반환
     */
    public async addComment(userData : UserData , quizIdx : QuizIdxParamReqType ,upsertCommnetReq : UpsertCommentReqType ) : Promise<CommentRes>{

        // 1. 새로운 entitiy 생성
        const comment : QuizCommentEntity  = this.quizCommentRepository.create({
            quizIdx:  quizIdx.quizIdx,
            content : upsertCommnetReq.content,
            state : upsertCommnetReq.state,
            userIdx : userData.userIdx,
            parent : upsertCommnetReq.parent? {idx:upsertCommnetReq.parent} : undefined,

        })

        // 2. 저장 
        const savedComment = await this.quizCommentRepository.save(comment);

        // 3. 닉네임 가져오기.

        // const userNickName = userData.userNickName ? userData.userNickName : "알 수 없음"  // 아래 코드로 단축 
        const userNickName = userData.userNickName || "알 수 없음"
        
        // 4. 반환 데이터 생성
        return toCommentRes(savedComment ,userNickName );
    }


    /**
     * 대댓글 조회
     * @param commentIdx 
     * @param pagingReq 
     * @returns Page<CommentListRes>
     */
    public async getCommentRepliesPage(commentIdx : CommentIdxParamReqType ,pagingReq : PagingReqType) :  Promise<Page<CommentListRes>>{

        // 1. 해당 댓글(commentIdx)의 대댓글 데이터 조회
        const pagingEntity : Page<QuizCommentEntity> = await this.quizCommentRepository.findCommentRepliesByPaging(commentIdx.commentIdx, pagingReq);

        // 2. 댓글 작성자들 nickname 조회 
        const commenters : number []  = pagingEntity.items.map((entity) => entity.userIdx);

        const usersRes : UserInfoListResponse = await this.userClient.getUserListInfo({userIdxs : commenters});


        // 3. idx와 닉네임을 매핑하여 dto에 추가
        // 3.1 <idx: nickname> 형태로 매핑
        const userNicknameMap: Record<number, string> = usersRes.users.reduce<Record<number, string>>((acc, user) => {
            acc[user.userIdx] = user.nickName;
            return acc;
        }, {} );
        
        // 4. 반환 데이터 생성
        const pageDTO : Page<CommentListRes> = toPageDTO(pagingEntity, (entity) => toCommentListRes(entity, userNicknameMap));
        return pageDTO;
    }


    /**
     * 댓글 수정
     * @param userData 
     * @param commentIdxReq 
     * @param upsertCommnetReq 
     * @returns 
     */
    public async updateComment(userData : UserData , commentIdxReq : CommentIdxParamReqType ,upsertCommnetReq : UpsertCommentReqType ) : Promise<CommentRes>{

        // 1. db 조회, 없을 시 에러
        const comment = await this.quizCommentRepository.findOneByOrFail({idx : commentIdxReq.commentIdx});

        // 2. 본인 확인!!!
        if(comment.userIdx !== userData.userIdx){
            throw new ForbiddenError("댓글 작성자만 수정 가능합니다")
        }

        // 3. entity 수정 , 부모에 대한 수정은 X >  실질적으로 대댓글을 수정 할 때 상위 댓글이 뭐인지를 바꾸진 않지..?
        comment.update(upsertCommnetReq.content, upsertCommnetReq.state);

        // 4. db 수정
        const updatedComment = await this.quizCommentRepository.save(comment);

        // 5. 닉네임 가져오기
        // const userNickName = userData.userNickName ? userData.userNickName : "알 수 없음"  // 아래 코드로 단축 
        const userNickName = userData.userNickName || "알 수 없음"
        
        // 6. 반환 데이터 생성
        return toCommentRes(updatedComment ,userNickName );


    }


    /**
     * 댓글 삭제
     * @param userData 
     * @param commentIdxReq 
     * @returns  commentIdx
     */
    public async deleteComment(userData : UserData , commentIdxReq : CommentIdxParamReqType) : Promise<CommentDeleteRes> {

        // 1. db 조회, 없을 시 에러
        const comment = await this.quizCommentRepository.findOneByOrFail({idx :commentIdxReq.commentIdx});

        // 2. 본인 확인
        if(comment.userIdx !== userData.userIdx){
            throw new ForbiddenError("댓글 작성자만 삭제 가능합니다")
        }

        // 3. soft delete
        await this.quizCommentRepository.softRemove(comment);

        // 4. 반환. 지금은 idx 만?
        return {commentIdx : commentIdxReq.commentIdx }
    }

}
