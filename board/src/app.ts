import 'reflect-metadata';
import express from 'express';
import config from './config';
import logger from './utils/logger'

async function startServer() {
    const app = express();
    
    const port = config.port

    await require('./loader').default({expressApp : app})
    app.listen(port , () => {
        logger.info("%d 번 포트로 웹 시작" , port);}
    );
}

startServer();