import express from 'express';
import expressLoader from './express';
import dbConnector from './dbConnector';
import dependencyInjector from './dependencyInjector';
import { AppDataSource } from './settingTypeORM'; 

export default async ({ expressApp }: { expressApp: express.Application } ) => {

    /** 0. Logger 생성 */
    

    
    // 1. db와 직접 연결 
    // const dbPool = await dbConnector();

    // 2. typeOrm 연결
    await AppDataSource.initialize();
    

    // DI 설정
    await dependencyInjector(AppDataSource);


    console.log("테스트 route 로드 시작")
    await expressLoader({app : expressApp})
    console.log("테스트 route 로드 완료")

}