import 'reflect-metadata';
import express from 'express';
import {logger} from './utils/logger'

async function startServer() {
    const app = express();
    
    await require('./loader').default({expressApp : app})
    app.listen(3000 , () => {
        logger.info('3000번 포트로 테스트 시작')}
    );
}

startServer();