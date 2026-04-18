import express from 'express';
import expressLoader from './express';
import dependencyInjector from './dependencyInjector';
import { AppDataSource } from './settingTypeORM'; 
import logger from '../utils/logger';



/**
 * 웹 실행 과정을 여기서 통재.
 * 필요한 모듈, 의존성 등을 여기서 순서대로 로드 
 */
export default async ({ expressApp }: { expressApp: express.Application } ) => {

    // 1. db와 직접 연결 
    // const dbPool = await dbConnector();

    // 1.1 typeOrm 연결
    logger.info("DB 로드 시작")
    await AppDataSource.initialize();
    logger.info("DB 로드 완료")

    // 2. DI 컨테이너 설정
    //  - 웹 실행 단계에서 이후 필요한 의존성을 미리 DI컨테이너에 넣어두는 과정
    //  - 현재는 DB 연결 풀 밖에 없는듯?
    logger.info("DI 컨테이너 로드 시작")
    await dependencyInjector(AppDataSource);
    logger.info("DI 컨테이너 로드 완료")

    // 3. router 로드 
    logger.info("router 로드 시작")
    await expressLoader({app : expressApp})
    logger.info("router 로드 완료")

}