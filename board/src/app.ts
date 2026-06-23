import 'reflect-metadata';
import express from 'express';
import config from './config';
import logger from './utils/logger'

async function startServer() {
    const app = express();
    
    const port = config.port

    // loader 호출
    await require('./loader').default({expressApp : app})

    // 웹 어플리케이션 시작
    app.listen(port , () => {
        logger.info("%d 번 포트로 웹 시작" , port);}
    );
}

startServer();