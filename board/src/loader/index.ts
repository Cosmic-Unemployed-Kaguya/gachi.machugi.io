import express from 'express';
import expressLoader from './express';
import dependencyInjector from './dependencyInjector';
import { AppDataSource } from './settingTypeORM'; 
import logger from '../utils/logger';
import {  addTransactionalDataSource, initializeTransactionalContext  }  from 'typeorm-transactional' ; 


/**
 * 웹 실행 과정을 여기서 통재.
 * 필요한 모듈, 의존성 등을 여기서 순서대로 로드 
 */
export default async ({ expressApp }: { expressApp: express.Application } ) => {

    // 1. db와 직접 연결 
    // const dbPool = await dbConnector();

    // 1.1 트랜잭션 컨텍스트 초기화
    // typeorm-transactional 패키지를 사용한 트랜잭션 구현
    // - 해당 패키지는 typeorm의 쿼리 코드를 런타임 시점에서 한번 덮어씌움.
    // - @Transacional이 붙은 메서드 실행 시 트랜잭션을 실행 할 manager 생성 후 이후 실행되는 쿼리를
    //   실행 전 낚아채 트랜잭션 내부에서 실행 시킴
    // - typeorm의 코드를 덮어씌우는것이기에 컨테이너 등록 전에 해당 트랜잭션컨텍스트를 초기화 할 필요가 있음
    initializeTransactionalContext();
    const appDataSource = AppDataSource;
    addTransactionalDataSource(appDataSource);

    // 1.2 typeOrm 연결
    logger.info("DB 로드 시작")
    await appDataSource.initialize();
    logger.info("DB 로드 완료")

    // 2. DI 컨테이너 설정
    //  - 웹 실행 단계에서 이후 필요한 의존성을 미리 DI컨테이너에 넣어두는 과정
    //  - 현재는 TypeORM 연결 풀 밖에 없는듯?
    logger.info("DI 컨테이너 로드 시작")
    await dependencyInjector(appDataSource);
    logger.info("DI 컨테이너 로드 완료")

    // 3. router 로드 
    logger.info("router 로드 시작")
    await expressLoader({app : expressApp})
    logger.info("router 로드 완료")

}